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
 * File: args.hpp                                                             *
 ******************************************************************************/

#ifndef ARGS_HPP
#define ARGS_HPP

#include <string>

struct args {
  std::string m_host;
  uint16_t    m_port;
  uint8_t     m_slot;
  bool        m_beacon;
};

struct arg_parser {
  /**
   * Parse arguments from given cstring
   * @returns `args` with parsed arguments
   */
  static args parse_args(int argc, char* argv[]);

  /**
   * Print help message
   */
  static void print_help();
};

#endif // ARGS_HPP

