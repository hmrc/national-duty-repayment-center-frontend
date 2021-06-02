/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.mappings

import java.time.LocalDate

import play.api.data.FormError
import play.api.data.format.Formatter
import LocalDateFormatter._

import scala.util.{Failure, Success, Try}

object LocalDateFormatter {
  val dayBlankErrorKey     = "error.date.day_blank"
  val dayInvalidErrorKey   = "error.date.day_invalid"
  val monthBlankErrorKey   = "error.date.month_blank"
  val monthInvalidErrorKey = "error.date.month_invalid"
  val yearBlankErrorKey    = "error.date.year_blank"
  val yearInvalidErrorKey  = "error.date.year_invalid"
}

private[mappings] class LocalDateFormatter(invalidKey: String, requiredKey: String)
    extends Formatter[LocalDate] with Formatters {

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(key, invalidKey)))
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    def bindIntSubfield(
      subKey: String,
      blankErrorKey: String,
      invalidErrorKey: String,
      extraValidation: Int => Boolean
    ) =
      intFormatter(requiredKey = blankErrorKey, wholeNumberKey = invalidErrorKey, nonNumericKey = invalidErrorKey).bind(
        s"$key.$subKey",
        data.mapValues(_.trim)
      )
        .right
        .flatMap(
          int =>
            if (extraValidation(int))
              Right(int)
            else
              Left(Seq(FormError(s"$key.$subKey", invalidErrorKey)))
        )

    val dayField = bindIntSubfield("day", dayBlankErrorKey, dayInvalidErrorKey, day => day >= 1 && day <= 31)

    val monthField =
      bindIntSubfield("month", monthBlankErrorKey, monthInvalidErrorKey, month => month >= 1 && month <= 12)

    val yearField = bindIntSubfield("year", yearBlankErrorKey, yearInvalidErrorKey, year => year >= 1)

    (dayField, monthField, yearField) match {
      case (Right(day), Right(month), Right(year)) => toDate(key, day, month, year)
      case (day, month, year)                      => Left(day.left.toSeq.flatten ++ month.left.toSeq.flatten ++ year.left.toSeq.flatten)
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day"   -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )

}
