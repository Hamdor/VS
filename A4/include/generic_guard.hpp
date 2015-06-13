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

template<typename F, typename X>
struct generic_guard {
  generic_guard(F fun, X a) : m_arg(a), m_fun(fun) {
    // nop
  }
  generic_guard(const generic_guard&) = delete;
  generic_guard(generic_guard&&) = default;
  ~generic_guard() {
    m_fun(m_arg);
  }
private:
  X m_arg;
  F m_fun;
};

template<typename F, typename X>
auto make_guard(F f, X x) {
  return generic_guard<F, X>(f,x);
}

