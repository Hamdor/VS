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
#define SLOT_TIME      (4LL)  //  4 msec
#define HALF_SLOT      (2LL)  //  2 msec
#define SECURITY_TIME2 (12LL) // 12 msec
#define NUM_SLOTS      (16)   // TODO: use in code too...
#define FULL_FRAME \
  (BEACON_WINDOW + SECURITY_TIME1 + NUM_SLOTS * SLOT_TIME + SECURITY_TIME2)

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

enum class state_t { INIT, WAITBEACON, WAITSLOT };

void run(args arg, int fd) {
  sigevent sigev;
  sigev.sigev_notify = SIGEV_THREAD_ID | SIGEV_SIGNAL;
  sigev.sigev_signo  = SIGALRM;
  sigev._sigev_un._tid = syscall(__NR_gettid);
  // Initialize sigset
  sigset_t sigset = { 0 };
  ccall(pred_zero, "add SIGINT failed",  sigaddset, &sigset, SIGINT);
  ccall(pred_zero, "add SIGIO failed",   sigaddset, &sigset, SIGIO);
  ccall(pred_zero, "add SIGALRM failed", sigaddset, &sigset, SIGALRM);
  ccall(pred_zero, "set SIG_BLOCK", sigprocmask, SIG_BLOCK, &sigset,
          nullptr);
  // Initialize Linux's HRT Timer
  timer_t timer;
  ccall(pred_zero, "timer_create failed!", timer_create, CLOCK_MONOTONIC,
        &sigev, &timer);
  auto timer_guard = make_guard(timer_delete, timer);
  // Values for computation...
  uint32_t beaconDelay = 0;
  timespec now = { 0 };
  itimerspec tspec = { 0 };
  char* sourceaddr = nullptr;
  int sourceport = 0;
  int signr = 0;
  uint64_t superframeStartTime = 0;
  uint64_t timeOffset = 0;
  uint64_t timeOffsetBeacon = 0;
  unsigned int frameCounter = 0;
  siginfo_t info = { 0 };
  // Allocate some buffers
  char recv_buffer[RECV_BUFFER_SIZE] = { 0 };
  char send_buffer[SEND_BUFFER_SIZE] = { 0 };
  char own_address[128] = { 0 };
  ccall(pred_zero, "gethostname failed", gethostname, own_address,
        sizeof(own_address));
  uint64_t timeToMiddleOfSlot = MSEC_TO_NSEC(BEACON_WINDOW + SECURITY_TIME1
                                             + SLOT_TIME * arg.m_slot
                                             - HALF_SLOT);
  state_t state = state_t::INIT;
  // Initialy wait 5 superframes for beacon...
  ccall(pred_zero, "clock_gettime failed!", clock_gettime, CLOCK_MONOTONIC,
        &now);
  nsec2timespec(&tspec.it_value, MSEC_TO_NSEC(5 * FULL_FRAME)
                + timespec2nsec(&now));
  ccall(pred_zero, "timer_settime failed!", timer_settime, timer, TIMER_ABSTIME,
        &tspec, nullptr);
  while(true) {
    signr = 0;
    while(signr == 0) {
      int err = recvMessage(fd, recv_buffer, RECV_BUFFER_SIZE, &sourceaddr,
                            &sourceport);
      if(err > 0) {
        signr = SIGIO;
        break;
      }
      ccall(pred_not_minus, "sigwaitinfo failed!", sigwaitinfo, &sigset, &info);
      if(info.si_signo == SIGALRM || info.si_signo == SIGINT) {
        signr = info.si_signo;
        break;
      }
    }
    if(signr == SIGINT) {
      cout << endl; // just for a cleaner output...
      return; // CTRL + C :-)
    }
    ccall(pred_zero, "clock_gettime failed!", clock_gettime, CLOCK_MONOTONIC,
          &now);
    switch(state) {
      case state_t::INIT: {
        if(signr == SIGALRM) {
          // Set values for slot timer
          superframeStartTime = timespec2nsec(&now);
          nsec2timespec(&tspec.it_value, superframeStartTime
                        + timeToMiddleOfSlot);
          ccall(pred_zero, "timer_settime failed!", timer_settime, timer,
                TIMER_ABSTIME, &tspec, nullptr);
          timeOffset = timespec2nsec(&now);
          state = state_t::WAITSLOT;
        } else if(signr == SIGIO) {
          if(recv_buffer[0] == 'B') {
            int rc = decodeBeacon(recv_buffer, &frameCounter, &beaconDelay,
                                  nullptr, 0);
            if(rc >= 0) {
              // first beacon, get the time...
              superframeStartTime = timespec2nsec(&now) - beaconDelay;
              nsec2timespec(&tspec.it_value, superframeStartTime
                            + timeToMiddleOfSlot);
              ccall(pred_zero, "timer_settime failed!", timer_settime, timer,
                    TIMER_ABSTIME, &tspec, nullptr);
              timeOffset = timespec2nsec(&now) - beaconDelay - frameCounter
                           * MSEC_TO_NSEC(FULL_FRAME);
              state = state_t::WAITSLOT;
            }
          }
        }
      } break;
      case state_t::WAITBEACON: {
        if(signr == SIGALRM) {
          // No beacon received, we can send our beacon :-)
          ++frameCounter;
          encodeBeacon(send_buffer, SEND_BUFFER_SIZE, frameCounter,
                       beaconDelay, own_address);
          sendMessage(fd, send_buffer, arg.m_host.c_str(), arg.m_port);
          // Set timer for slot send time
          superframeStartTime = (frameCounter * MSEC_TO_NSEC(FULL_FRAME))
                                + timeOffset;
          nsec2timespec(&tspec.it_value, superframeStartTime
                        + timeToMiddleOfSlot);
          ccall(pred_zero, "timer_settime failed!", timer_settime, timer,
                TIMER_ABSTIME, &tspec, nullptr);
          state = state_t::WAITSLOT;
        } else if(signr == SIGIO) {
          if(recv_buffer[0] == 'B') {
            // We just received a beacon, this means we don't have
            // to send our beacon...
            int rc = decodeBeacon(recv_buffer, &frameCounter, &beaconDelay,
                                  nullptr, 0);
            if( rc < 0 ) {
              cout << "### Invalid Beacon: " << recv_buffer << endl;
            } else {
              // Beacon is valid, we can set the time offset
              timeOffsetBeacon = timespec2nsec(&now) -
                MSEC_TO_NSEC(frameCounter * FULL_FRAME) - beaconDelay;
              // We have to adjust the offset ONLY if the received offset is
              // bigger this means the start time was earlier...
              if(timeOffsetBeacon < timeOffset) {
                timeOffset = timeOffsetBeacon;
              }
              // Correct the slot send timer
              superframeStartTime = MSEC_TO_NSEC(frameCounter * FULL_FRAME)
                                                 + timeOffset;
              nsec2timespec(&tspec.it_value, superframeStartTime
                            + timeToMiddleOfSlot);
              ccall(pred_zero, "timer_settime failed!", timer_settime, timer,
                    TIMER_ABSTIME, &tspec, nullptr);
              state = state_t::WAITSLOT;
            }
          }
        }
      } break;
      case state_t::WAITSLOT: {
        if(signr == SIGALRM) {
          // Time to send in our slot!
          encodeSlotMessage(send_buffer, SEND_BUFFER_SIZE, arg.m_slot,
                            own_address);
          sendMessage(fd, send_buffer, arg.m_host.c_str(), arg.m_port);
          // We have to calculate a new random value for the next timepoint
          // to send the beacon...
          beaconDelay = randomNumber(MSEC_TO_NSEC(BEACON_WINDOW));
          nsec2timespec(&tspec.it_value, superframeStartTime +
                        MSEC_TO_NSEC(FULL_FRAME) + beaconDelay);
          // Set the timer...
          ccall(pred_zero, "timer_settime failed!", timer_settime, timer,
                TIMER_ABSTIME, &tspec, nullptr);
          state = state_t::WAITBEACON;
        }
      } break;
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
