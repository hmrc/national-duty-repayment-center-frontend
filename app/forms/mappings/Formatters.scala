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

import forms.{TrimWhitespace, Validation}
import models.Enumerable
import play.api.data.format.Formatter
import play.api.data.{FormError, Mapping}

import scala.util.control.Exception.nonFatalCatch
import scala.util.{Failure, Success, Try}

trait Formatters extends TrimWhitespace {

  private[mappings] def stringFormatter(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                              => Left(Seq(FormError(key, errorKey)))
        case Some(value) if value.trim.isEmpty => Left(Seq(FormError(key, errorKey)))
        case Some(s)                           => Right(s.trim)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value.trim)

  }

  private[mappings] def stringFormatterNoSpaces(errorKey: String): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                                         => Left(Seq(FormError(key, errorKey)))
        case Some(value) if trimWhitespace(value).isEmpty => Left(Seq(FormError(key, errorKey)))
        case Some(s)                                      => Right(trimWhitespace(s))
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> trimWhitespace(value))

  }

  private[mappings] def booleanFormatter(requiredKey: String, invalidKey: String): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .right.flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey)))
          }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(
    requiredKey: String,
    wholeNumberKey: String,
    nonNumericKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[Int] =
    new Formatter[Int] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .right.map(_.replace(",", ""))
          .right.flatMap {
            case s if trimWhitespace(s).matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s =>
              nonFatalCatch
                .either(trimWhitespace(s).toInt)
                .left.map(_ => Seq(FormError(key, nonNumericKey, args)))
          }

      override def unbind(key: String, value: Int) =
        baseFormatter.unbind(key, value.toString)

    }

  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String)(implicit
    ev: Enumerable[A]
  ): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).right.flatMap {
          str =>
            ev.withName(str).map(Right.apply).getOrElse(Left(Seq(FormError(key, invalidKey))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)

    }

  private[mappings] def decimalFormatter(requiredKey: String, nonNumericKey: String): Formatter[String] =
    new Formatter[String] {

      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        baseFormatter.bind(key, data)
          .right.map(_.replace(",", ""))
          .right.map(_.replace("£", ""))
          .right.flatMap {
            s =>
              val valueNoSpaces = trimWhitespace(s)
              Try(valueNoSpaces.toDouble) match {
                case Success(_) => Right(valueNoSpaces)
                case Failure(_) => Left(Seq(FormError(key, nonNumericKey)))
              }
          }

      override def unbind(key: String, value: String): Map[String, String] =
        baseFormatter.unbind(key, value)

    }

  def emailAddressMapping(
    keyLength: String,
    keyInvalid: String,
    keyRequired: String,
    keySelectionRequired: String
  ): Mapping[Option[String]] = {

    val emailFieldName     = "email"
    val selectionFieldName = "value"

    def bind(data: Map[String, String]): Either[Seq[FormError], Option[String]] = {

      val emailAddress = data.get(emailFieldName)
      val useEmail     = data.get("value")

      val maxLengthEmailAddress = 85

      (emailAddress, useEmail) match {
        case (Some(""), Some("01")) => Left(Seq(FormError(emailFieldName, keyRequired)))
        case (_, None)              => Left(Seq(FormError(selectionFieldName, keySelectionRequired)))
        case (Some(email), Some("01")) if email.length > 0 && email.length > maxLengthEmailAddress =>
          Left(Seq(FormError(emailFieldName, keyLength)))
        case (Some(email), Some("01")) if !email.matches(Validation.emailRegex) =>
          Left(Seq(FormError(emailFieldName, keyInvalid)))
        case (Some(email), Some("01")) => Right(Some(email))
        case _                         => Right(None)
      }

    }

    def unbind(value: Option[String]): Map[String, String] =
      Map(emailFieldName -> value.getOrElse(""))

    new CustomBindMapping(emailFieldName, bind, unbind)

  }

}
