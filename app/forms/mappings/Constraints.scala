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

package forms.mappings

import java.time.LocalDate
import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.util.{Success, Try}

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev._

        if (input >= minimum)
          Valid
        else
          Invalid(errorKey, minimum)
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev._

        if (input <= maximum)
          Valid
        else
          Invalid(errorKey, maximum)
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.trim.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def regexp(regex: String, errorKey: String, transform: String => String = x => x): Constraint[String] =
    Constraint {
      case str if transform(str).matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(
    maximum: Int,
    errorKey: String,
    transformation: String => String = identity
  ): Constraint[String] =
    Constraint {
      case str if transformation(str.trim).length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def minLength(minimum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.trim.length >= minimum =>
        Valid
      case _ =>
        Invalid(errorKey, minimum)
    }

  protected def exactLength(length: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.trim.length == length =>
        Valid
      case _ =>
        Invalid(errorKey, length)
    }

  protected def startsWith(errorKey: String): Constraint[String] =
    Constraint {
      case str if str.trim.toUpperCase.startsWith("NDRC") =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def maxDateToday(errorKey: String): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(LocalDate.now()) =>
        Invalid(errorKey)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def greaterThanZero(errorKey: String): Constraint[String] =
    Constraint {
      input =>
        Try(BigDecimal(input)) match {
          case Success(value) if value > 0 => Valid
          case _                           => Invalid(errorKey)
        }
    }

  protected def greaterThanOrEqualZero(errorKey: String): Constraint[String] =
    Constraint {
      input =>
        Try(BigDecimal(input)) match {
          case Success(value) if value >= 0 => Valid
          case _                            => Invalid(errorKey)
        }
    }

}
