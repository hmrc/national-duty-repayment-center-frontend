/*
 * Copyright 2020 HM Revenue & Customs
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

package forms

import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.util.matching.Regex

object Validation {

  val phoneNumberRegex              = """^\+[0-9 ]{1,18}$|^[0-9 ]{1,19}$|^(?=.{2,22}$)\+[0-9 ]*\(0\)[0-9 ]*$|^(?=.{1,22}$)[0-9 ]*\(0\)[0-9 ]*$"""
  val emailRegex                    = """^.+[@].+[.].+$"""
  val utrPattern: Regex             = "\\d{10}".r.anchored
  val postcodeRegex                 = """^[ ]*[A-Za-z][ ]*[A-Za-z]{0,1}[ ]*[0-9][ ]*[0-9A-Za-z]{0,1}[ ]*[0-9][ ]*[A-Za-z][ ]*[A-Za-z][ ]*$"""
  val safeInputPattern              = """^[A-Za-z0-9À-ÿ \!\)\(.,_/’'"&-]+$"""
  val accountNumberPattern: Regex   = "^[ -]*(?:\\d[ -]*){6,8}$".r.anchored
  val sortCodePattern: Regex        = "^[ -]*(?:\\d[ -]*){6,6}$".r.anchored
  val vatRegistrationPattern: Regex = "(?:[Gg][Bb])?\\d{9}".r.anchored
  val payeReferencePattern: Regex   = """\d{3}/[A-Za-z0-9]{1,10}""".r.anchored
  val rollNumberPattern             = """[a-zA-Z0-9- .]{1,18}""".r.anchored

  def emailConstraint(errorKey: String): Constraint[String] = {
    Constraint[String]((value: String) => if (EmailAddress.isValid(value) && !value.contains("|")) Valid else Invalid(errorKey))
  }
}
