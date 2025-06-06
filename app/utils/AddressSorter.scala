/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import models.responses.LookedUpAddressWrapper

import javax.inject.Singleton
import scala.annotation.tailrec

@Singleton
class AddressSorter {

  private def zipWithNaturalOrderable[T](getSource: T => String)(in: T): (T, String) = {

    @tailrec
    def loop(in: Seq[Char], charsOut: List[Char], intsOut: List[Int], lastWasInt: Boolean): List[Char] =
      if (in.isEmpty) intsOut.reverse.map(_.toChar) ++ charsOut.reverse
      else if (in.head.isDigit)
        if (lastWasInt)
          loop(in.tail, charsOut, ((intsOut.head * 10) + in.head.getNumericValue) :: intsOut.tail, lastWasInt = true)
        else
          loop(in.tail, charsOut, in.head.getNumericValue :: intsOut, lastWasInt = true)
      else
        loop(in.tail, in.head :: charsOut, intsOut, lastWasInt = false)

    val sanitisedSource = getSource(in).toLowerCase.toCharArray
    (in, loop(sanitisedSource.toIndexedSeq, Nil, Nil, lastWasInt = false).mkString)
  }

  def sort(addresses: Seq[LookedUpAddressWrapper]): Seq[LookedUpAddressWrapper] = {

    val zipFunc = zipWithNaturalOrderable[LookedUpAddressWrapper](_.address.lines.take(2).reverse.mkString) _
    addresses.map(zipFunc).sortBy(_._2).map(_._1)
  }

}
