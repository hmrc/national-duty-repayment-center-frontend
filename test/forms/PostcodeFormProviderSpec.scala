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
class PostcodeFormProviderSpec extends StringFieldBehaviours {
  val requiredKey = "postcode.error.required"
  val lengthKey = "postcode.error.length"
  val minLength = 6
  val maxLength = 9
  val form = new PostcodeFormProvider()()
  ".value" must {
    val fieldName = "PostalCode"
    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMinAndMaxLength(minLength,maxLength)
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
  }
  ".value" must {
    val fieldName = "PostalCode"
    "not bind postcode with less than 6 digit" in {
      val result = form.bind(Map(fieldName -> "AA 1A")).apply(fieldName)
      result.errors shouldEqual Seq(
        FormError(fieldName, lengthKey, Seq(minLength))
      )
    }
    "not bind postcode with more than 9 digit" in {
      val result = form.bind(Map(fieldName -> "AA111 1AAAA")).apply(fieldName)
      result.errors shouldEqual Seq(
        FormError(fieldName, lengthKey, Seq(maxLength))
      )
    }
  }
}