/******************************************************************************
 *                __  __  ____        ______  __ __                           *
 *               /\ \/\ \/\  _`\     /\  _  \/\ \\ \                          *
 *               \ \ \ \ \ \,\L\_\   \ \ \L\ \ \ \\ \                         *
 *                \ \ \ \ \/_\__ \    \ \  __ \ \ \\ \_                       *
 *                 \ \ \_/ \/\ \L\ \   \ \ \/\ \ \__ ,__\                     *
 *                  \ `\___/\ `\____\   \ \_\ \_\/_/\_\_/                     *
 *                   `\/__/  \/_____/    \/_/\/_/  \/_/                       *
 *                                                                            *
 *                                                                            *
 * Marian Triebe    <marian.triebe   (at) haw-hamburg.de>                     *
 * Moritz Heindorf  <moritz.heindorf (at) haw-hamburg.de>                     *
 *                                                                            *
 * VS Gruppe 2 - SS 2015                                                      *
 * File: main.cpp                                                             *
 ******************************************************************************/

#include <signal.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <sys/types.h>

#include <cstring>
#include <sstream>
#include <iostream>
#include <stdexcept>

#include "args.hpp"
#include "datagram.h"
#include "generic_guard.hpp"

using namespace std;

#define SEND_BUFFER_SIZE (256)
#define RECV_BUFFER_SIZE (256)

#define BEACON_WINDOW  (20LL) // 20 msec
#define SECURITY_TIME1 (4LL)  //  4 msec
#define SLOT           (4LL)  //  4 msec
#define SECURITY_TIME2 (12LL) // 12 msec
#define NUM_SLOTS      (16)   // TODO: use in code too...
#define FULL_FRAME \
  (BEACON_WINDOW + SECURITY_TIME1 + NUM_SLOTS * SLOT + SECURITY_TIME2)

#define MSEC_TO_NSEC(msec) ((msec) * 1000 * 1000)

namespace {

constexpr uint64_t full_frame_msec = FULL_FRAME;
constexpr uint64_t full_frame_nsec = MSEC_TO_NSEC(full_frame_msec);

// Check if full_frame_nsec is bigger than full_frame_msec
// if this is not true there was an overflow and FULL_FRAME is too big to
// fit in an uint64_t in nsec...
static_assert(full_frame_nsec > full_frame_msec, "overflow in time specifications");

} // namespace <anonymous>

bool pred_zero(int value) {
  return value == 0;
}

bool pred_not_minus(int value) {
  return value >= 0;
}

// Error checking for C calls...
template <class Predicate, class F, class... Ts>
auto ccall(Predicate p, const char* errmsg, F f, Ts&&... xs) {
  auto result = f(forward<Ts>(xs)...);
  if (!p(result)) {
    ostringstream oss;
    oss << errmsg << ": " << result;
    throw runtime_error(oss.str());
  }
  return result;
}

void run(args arg, int fd) {
  auto init_sigev = [](auto signal) {
    sigevent sig = { 0 };
    sig.sigev_notify   = SIGEV_THREAD_ID | SIGEV_SIGNAL;
    sig.sigev_signo    = signal;
    sig._sigev_un._tid = syscall(__NR_gettid);
    return sig;
  };
  sigevent sigev_slot   = init_sigev(SIGUSR1); // Init sigevent for slot
  sigevent sigev_beacon = init_sigev(SIGALRM); // Init sigevent for beacon
  // Initalize sigset
  sigset_t sigset = { 0 };
  ccall(pred_zero, "add SIGINT failed",  sigaddset, &sigset, SIGINT);
  ccall(pred_zero, "add SIGIO failed",   sigaddset, &sigset, SIGIO);
  ccall(pred_zero, "add SIGALRM failed", sigaddset, &sigset, SIGALRM);
  ccall(pred_zero, "add SIGUSR1 failed", sigaddset, &sigset, SIGUSR1);
  ccall(pred_zero, "set SIG_BLOCK", sigprocmask, SIG_BLOCK, &sigset,
        nullptr);
  // Initialize Linux's HRT Timer
  timer_t timer_beacon;
  timer_t timer_slot;
  ccall(pred_zero, "Init timer_beacon failed", timer_create,
        CLOCK_MONOTONIC, &sigev_beacon, &timer_beacon);
  auto timer_guard_beacon = make_guard(timer_delete, timer_beacon);
  ccall(pred_zero, "Init timer_slot failed", timer_create, CLOCK_MONOTONIC,
        &sigev_slot, &timer_slot);
  auto timer_guard_slot = make_guard(timer_delete, timer_slot);
  // Set time TODO: Set correct time
  itimerspec spec_beacon = { 0, full_frame_nsec, 0, full_frame_nsec };
  ccall(pred_zero, "set time failed", timer_settime, timer_beacon, 0,
        &spec_beacon, nullptr);
  // Allocate some buffers
  char recv_buffer[RECV_BUFFER_SIZE] = { 0 };
  char send_buffer[SEND_BUFFER_SIZE] = { 0 };
  char* source_addr = nullptr;
  int   source_port = 0;
  char own_address[128] = { 0 };
  ccall(pred_zero, "gethostname failed", gethostname, own_address,
        sizeof(own_address));
  timespec current = { 0 };
  uint32_t frame_no = 0;
  uint32_t beacon_delay = MSEC_TO_NSEC(BEACON_WINDOW + SECURITY_TIME1);
  // Handle signals
  bool run = true;
  siginfo_t info = { 0 };
  int signal = 0;
  while (run) {
    signal = 0;
    memset(&info, 0, sizeof(siginfo_t));
    memset(recv_buffer, 0, RECV_BUFFER_SIZE);
    // SIGIO is edge sensitive, we have to ensure that the receive
    // buffer is already empty, or we will loose a signal
    while (signal == 0) {
      int err = recvMessage(fd, recv_buffer, RECV_BUFFER_SIZE, &source_addr,
                  &source_port);
      if (err > 0) {
        signal = SIGIO;
        break;
      }
      ccall(pred_not_minus, "sigwaitinfo failed", sigwaitinfo, &sigset, &info);
      if (  info.si_signo == SIGINT || info.si_signo == SIGUSR1
         || info.si_signo == SIGALRM) {
        signal = info.si_signo;
        break;
      }
    }
    // Get current time...
    ccall(pred_zero, "clock_gettime failed", clock_gettime, CLOCK_MONOTONIC,
          &current);
    switch (info.si_signo) {
      case SIGINT: // CTRL + C (Exit program)
        cout << endl; // just for a cleaner output...
        run = false;
        return;
      break;
      case SIGIO: // New IO from socket
        cout << recv_buffer << endl;
        if (recv_buffer[0] == 'B') {
          // Received a Beacon message
        } else if (recv_buffer[0] == 'D') {
          // Received a slot message
        } else {
          // nop
        }
      break;
      case SIGUSR1: // Timer invoked SIGUST1 (Time to send in our slot)
      break;
      case SIGALRM: // Timer invoked SIGALRM (Time to send the beacon)
        encodeBeacon(send_buffer, SEND_BUFFER_SIZE, frame_no++, beacon_delay, own_address);
        sendMessage(fd, send_buffer, arg.m_host.c_str(), arg.m_port);
      break;
    }
  }
}

int main(int argc, char* argv[]) {
  auto resu = arg_parser::parse_args(argc, argv);
  auto fd = initSocket(resu.m_host.c_str(), resu.m_port);
  if (fd < 0) {
    cout << "Error initializing socket... exit..." << endl;
    return 1;
  }
  auto fd_guard = make_guard(close, fd);
  try {
    run(resu, fd);
  } catch(exception& e) {
    cout << e.what() << endl;
  }
  cout << "About to leave " << __PRETTY_FUNCTION__ << "..." << endl;
}

