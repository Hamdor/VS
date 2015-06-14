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
#include <iostream>

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

void run(args arg, int fd) {
  int err = 0; // TODO: Add error checking and return...
  auto specific_sig = (arg.m_beacon ? SIGALRM : SIGUSR1);
  // Initialize sigev for later timer usage
  sigevent sigev;
  sigev.sigev_notify   = SIGEV_THREAD_ID | SIGEV_SIGNAL;
  sigev.sigev_signo    = specific_sig;
  sigev._sigev_un._tid = syscall(__NR_gettid); // sadly, there seems to be no
                                               // C++ function which gives us
                                               // the thread id as int...
  // Initalize sigset
  sigset_t sigset;
  sigemptyset(&sigset);
  sigaddset(&sigset, SIGINT);       // Signal triggered from CTRL + C
  sigaddset(&sigset, SIGIO);        // Socket received data
  sigaddset(&sigset, specific_sig); // Specific SIG (SIGALARM or SIGUSR1)
  sigprocmask(SIG_BLOCK, &sigset, NULL);
  // Initialize Linux's HRT Timer
  timer_t timer;
  err = timer_create(CLOCK_MONOTONIC, &sigev, &timer);
  auto timer_guard = make_guard(timer_delete, timer);
  // Set time TODO: Set correct time
  itimerspec spec = { 0, full_frame_nsec, 0, full_frame_nsec };
  timer_settime(timer, 0, &spec, nullptr);
  // Allocate some buffers
  char recv_buffer[RECV_BUFFER_SIZE] = { 0 };
  char send_buffer[SEND_BUFFER_SIZE] = { 0 };
  char* source_addr = nullptr;
  int   source_port = 0;
  char own_address[128] = { 0 };
  err = gethostname(own_address, sizeof(own_address));
  cout << "Hostname: " << own_address << ":" << arg.m_port << endl;
  timespec current = { 0 };
  // Handle signals
  bool run = true;
  siginfo_t info = { 0 };
  int signal = 0;
  while (run) {
    signal = 0;
    memset(&info, 0, sizeof(siginfo_t));
    // SIGIO is edge sensitive, we have to ensure that the receive
    // buffer is already empty, or we will loose a signal
    while (signal == 0) {
      err = recvMessage(fd, recv_buffer, RECV_BUFFER_SIZE, &source_addr,
                        &source_port);
      if (err > 0) {
        signal = SIGIO;
        break;
      }
      sigwaitinfo(&sigset, &info);
      if (  info.si_signo == SIGINT || info.si_signo == SIGUSR1
         || info.si_signo == SIGALRM) {
        signal = info.si_signo;
        break;
      }
    }
    // Get current time...
    switch (info.si_signo) {
      case SIGINT:
        // Break life loop, this will cause the program to terminate
        run = false;
        return;
      break;
      case SIGIO:
        cout << "source: " << source_addr << ":" << source_port << endl;
        // New IO from socket
        if (recv_buffer[0] == 'B') {
          // Received a Beacon message
          cout << "received a beacon messgae" << endl << recv_buffer << endl;
        } else if (recv_buffer[0] == 'D') {
          // Received a slot message
          cout << "received a slot message" << endl << recv_buffer << endl;
        } else {
          // nop
          cout << "Unknown begin of message" << endl << recv_buffer << endl;
        }
      break;
      case SIGUSR1:
        // Timer invoked SIGUSR1 => Time to send in our slot
      break;
      case SIGALRM:
        // Timer invoked SIGALRM => Time to send Beacon
        clock_gettime(CLOCK_MONOTONIC, &current);
        send_buffer[0] = 'B';
        cout << sendMessage(fd, send_buffer, arg.m_host.c_str(), arg.m_port) << endl;
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
  cout << "fd: " << fd << endl;
  auto fd_guard = make_guard(close, fd);
  run(resu, fd);
  cout << "\nAbout to leave main..." << endl;
}

