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

package forms

import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation._
import play.api.data.{Form, Mapping}

class AdditionalFileUploadFormProvider {
  def constraint[A](fieldName: String, errorType: String, predicate: A => Boolean): Constraint[A] =
    Constraint[A](s"constraint.$fieldName.$errorType") { s =>
      Option(s)
        .filter(predicate)
        .fold[ValidationResult](Invalid(ValidationError(s"error.$fieldName.$errorType")))(_ => Valid)
    }
  def booleanMapping(fieldName: String, trueValue: String, falseValue: String): Mapping[Boolean] =
    optional(text)
      .verifying(constraint[Option[String]](fieldName, "required", _.exists(s => s == trueValue || s == falseValue)))
      .transform[Boolean](_.contains(trueValue), b => if (b) Some(trueValue) else Some(falseValue))
  val uploadAnotherFileMapping: Mapping[Boolean] = booleanMapping("uploadAnotherFile", "yes", "no")

  val UploadAnotherFileChoiceForm = Form[Boolean](
    mapping("uploadAnotherFile" -> uploadAnotherFileMapping)(identity)(Option.apply)
  )
}
