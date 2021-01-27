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
import org.scalacheck.Gen
import play.api.data.FormError

class EmailAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "emailAddress.error.required"
  val lengthKey = "emailAddress.error.length"
  val invalidKey = "emailAddress.error.invalid"
  val maxLength = 85

  val form = new EmailAddressFormProvider()()

  ".email" must {

    val fieldName = "email"
    val fieldName2 = "value"
    val fieldValueYes = "01"

    val basicEmail            = Gen.const("foo@example.com")
    val emailWithSpecialChars = Gen.const("aBcD.!#$%&'*+/=?^_`{|}~-123@foo-bar.example.com")
    val validData             = Gen.oneOf(basicEmail, emailWithSpecialChars)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    behave like fieldWithMaxLengthCombo(
      form,
      fieldName,
      fieldName2,
      fieldValueYes,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey)
    )

  }
}
