/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class DeclarantNameFormProviderSpec extends StringFieldBehaviours {

  val requiredFirstNameKey  = "declarantName.firstName.error.required"
  val requiredLastNameKey   = "declarantName.lastName.error.required"
  val lengthFirstNameKey    = "declarantName.firstName.error.length"
  val lengthLastNameNameKey = "declarantName.lastName.error.length"
  val firstNameInvalidKey   = "declarantName.firstName.error.invalid"
  val lastNameInvalidKey    = "declarantName.lastName.error.invalid"
  val maxLength             = 255

  val form = new DeclarantNameFormProvider()()

  ".firstName" must {

    val fieldName = "firstName"

    behave like fieldThatBindsValidData(form, fieldName, stringsWithMaxLengthAlpha(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthFirstNameKey, Seq(maxLength))
    )

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredFirstNameKey))

  }

  ".lastName" must {

    val fieldName = "lastName"

    behave like fieldThatBindsValidData(form, fieldName, stringsWithMaxLengthAlpha(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthLastNameNameKey, Seq(maxLength))
    )

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredLastNameKey))

  }
}
