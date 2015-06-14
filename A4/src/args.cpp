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
 * File: args.cpp                                                             *
 ******************************************************************************/

#include <getopt.h>

#include <limits>
#include <cstdint>
#include <iostream>

#include "args.hpp"

using namespace std;

namespace {

enum options {
  host = 1,
  port,
  slot,
  beacon,
  help
};

const struct option options[] = {
  { "host",   required_argument, 0, static_cast<char>(host)   },
  { "port",   required_argument, 0, static_cast<char>(port)   },
  { "slot",   required_argument, 0, static_cast<char>(slot)   },
  { "beacon", no_argument,       0, static_cast<char>(beacon) },
  { "help",   no_argument,       0, static_cast<char>(help)   }
};

} // namespace <anonymous>

void arg_parser::print_help() {
  cout << "Usage: ./a4 [Options]"                                       << endl
       << "Options:"                                                    << endl
       << "  --host=<arg> Set the multicast address (def: 224.0.0.251)" << endl
       << "  --port=<arg> Set the port (def: 1337)"                     << endl
       << "  --slot=<arg> Set the slot to send on (1 - 16) (def: 1)"    << endl
       << "  --beacon     Indicates send beacon (def: false)"           << endl
       << "  --help       Show this help message"                       << endl;
}

args arg_parser::parse_args(int argc, char* argv[]) {
  args res = { "224.0.0.251", 1337, 1, false };
  int opt = 0;
  int idx = 0;
  auto get_range = [&](auto lower, auto upper) {
    auto tmp = atoi(optarg);
    return tmp >= lower && tmp <= upper ? tmp : 0;
  };
  while ((opt = getopt_long_only(argc, argv, "", options, &idx)) != -1) {
    switch (opt) {
      case host: {
        res.m_host = optarg;
      } break;
      case port: {
        res.m_port = get_range(numeric_limits<uint16_t>::min(),
                               numeric_limits<uint16_t>::max());
      } break;
      case slot: {
        res.m_slot = get_range(numeric_limits<uint8_t>::min(),
                               numeric_limits<uint8_t>::max());
      } break;
      case beacon: {
        res.m_beacon = true;
      } break;
      case help: {
        print_help();
        exit(-1);
      } break;
    }
  }
  return res;
}
