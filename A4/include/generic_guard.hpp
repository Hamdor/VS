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
 * File: generic_guard.hpp                                                    *
 ******************************************************************************/

#ifndef GENERIC_GUARD_HPP
#define GENERIC_GUARD_HPP

#define DEBUG

#ifdef DEBUG
#include <iostream>
#endif

template<typename F, typename X>
struct generic_guard {
  generic_guard(F fun, X a) : m_arg(a), m_fun(fun) {
#ifdef DEBUG
    std::cout << __PRETTY_FUNCTION__ << std::endl;
#endif
  }
  generic_guard(const generic_guard&) = delete;
  generic_guard(generic_guard&&) = default;
  ~generic_guard() {
    m_fun(m_arg);
#ifdef DEBUG
    std::cout << __PRETTY_FUNCTION__ << std::endl; 
#endif
  }
private:
  X m_arg;
  F m_fun;
};

template<typename F, typename X>
auto make_guard(F f, X x) {
  return generic_guard<F, X>(f,x);
}

#endif // GENERIC_GUARD_HPP

