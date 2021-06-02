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

import forms.mappings.Mappings
import models.{AmendCaseResponseType, IsContactProvided}
import play.api.data.{Form, Mapping}
import play.api.data.Forms.{mapping, set}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.{Condition, ConditionalMapping, MandatoryOptionalMapping}
import uk.gov.voa.play.form.ConditionalMappings.{
  mandatoryAndOnlyIfAnyOf,
  mandatoryIfEqual,
  mandatoryIfEqualToAny,
  mandatoryIfTrue
}

import javax.inject.Inject

case class EmailAndPhoneNumber(emailOrPhone: Set[IsContactProvided], email: Option[String], phone: Option[String])

object EmailAndPhoneNumber {
  implicit val format: OFormat[EmailAndPhoneNumber] = Json.format[EmailAndPhoneNumber]
}

class EmailAddressAndPhoneNumberFormProvider @Inject() extends Mappings {

  def contains(field: String, elem: String): Condition = x => {
    val t = x.values.exists(_ == elem)
    t
  }

  def mandatoryIfContains[T](condition: Condition, mapping: Mapping[T]) =
    ConditionalMapping(condition, MandatoryOptionalMapping(mapping, Nil), None, Seq.empty)

  def apply(): Form[EmailAndPhoneNumber] =
    Form(
      mapping(
        "value" -> set(enumerable[IsContactProvided]("isContactProvided.error.required")).verifying(
          nonEmptySet("isContactProvided.error.required")
        ),
        "email" -> mandatoryIfContains(
          contains("value", "01"),
          textNoSpaces("emailAddress.error.required")
            .verifying(
              firstError(
                maxLength(85, "emailAddress.error.length"),
                regexp(Validation.emailRegex, "emailAddress.error.invalid")
              )
            )
        ),
        "phone" -> mandatoryIfContains(
          contains("value", "02"),
          textNoSpaces("phoneNumber.error.required")
            .verifying(
              firstError(
                maxLength(11, "phoneNumber.error.length"),
                regexp(Validation.phoneNumberPattern, "phoneNumber.error.invalid")
              )
            )
        )
      )(EmailAndPhoneNumber.apply)(EmailAndPhoneNumber.unapply)
    )

}
