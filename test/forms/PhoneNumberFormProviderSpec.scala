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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class PhoneNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "phoneNumber.error.required"
  val lengthKey = "phoneNumber.error.length"
  val invalidKey = "phoneNumber.error.invalid"
  val maxLength = 11

  val form = new PhoneNumberFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "bind values with correct phone number format" in {
      val result = form.bind(Map("value" -> "07486236608")).apply(fieldName)
      result.value.get shouldBe "07486236608"
      result.errors shouldBe List.empty
    }

    "bind values with wrong phone number format" in {
      val result = form.bind(Map("value" -> "S7486236608")).apply(fieldName)
      result.errors shouldEqual Seq(
        FormError(fieldName, invalidKey, List(forms.Validation.phoneNumberPattern))
      )
    }
  }
}
