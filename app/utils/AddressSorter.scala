/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package utils

import javax.inject.Singleton
import models.responses.LookedUpAddressWrapper

import scala.annotation.tailrec

@Singleton
class AddressSorter {

  private def zipWithNaturalOrderable[T](getSource: T => String)(in: T): (T, String) = {

    @tailrec
    def loop(in: Seq[Char], charsOut: List[Char], intsOut: List[Int], lastWasInt: Boolean): List[Char] = {
      if (in.isEmpty) { intsOut.reverse.map(_.toChar) ++ charsOut.reverse }
      else {
        if (in.head.isDigit) {
          if (lastWasInt) {
            loop(in.tail, charsOut, ((intsOut.head * 10) + in.head.getNumericValue) :: intsOut.tail, lastWasInt = true)
          } else {
            loop(in.tail, charsOut, in.head.getNumericValue :: intsOut, lastWasInt = true)
          }
        } else {
          loop(in.tail, in.head :: charsOut, intsOut, lastWasInt = false)
        }
      }
    }

    val sanitisedSource = getSource(in).toLowerCase.toCharArray
    (in, loop(sanitisedSource, Nil, Nil, lastWasInt = false).mkString)
  }

  def sort(addresses: Seq[LookedUpAddressWrapper]): Seq[LookedUpAddressWrapper] = {

    val zipFunc = zipWithNaturalOrderable[LookedUpAddressWrapper](_.address.lines.take(2).reverse.mkString) _
    addresses.map(zipFunc).sortBy(_._2).map(_._1)
  }
}
