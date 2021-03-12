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

class ReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "referenceNumber.error.required"
  val lengthKey = "referenceNumber.error.length"
  val invalidChars = "referenceNumber.error.invalid.chars"
  val ndrcPrefixKey = "referenceNumber.error.prefix"

  val maxLength = 23

  val form = new ReferenceNumberFormProvider()()

  ".value" must {

    val fieldName = "value"

    "bind valid data" in {

      forAll(stringsWithMaxLengthAlpha(maxLength - 4) -> "validDataItem") {
        dataItem: String =>
          val result = form.bind(Map(fieldName -> ("ndrc" + dataItem))).apply(fieldName)
          result.value.value shouldBe ("ndrc" + dataItem)
      }
    }

    s"not bind strings longer than $maxLength characters" in {
      forAll(stringsLongerThanAlpha(maxLength - 4) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> ("ndrc" + string))).apply(fieldName)
          result.errors shouldEqual Seq( FormError(fieldName, lengthKey, Seq(maxLength)))
      }
    }

    s"not bind strings with special characters" in {
      forAll(stringsWithMaxLength(maxLength) -> "specialCharStrings") {
        string =>
          val result = form.bind(Map(fieldName -> (string))).apply(fieldName)
          result.errors shouldEqual Seq( FormError(fieldName, invalidChars, Seq("^[a-zA-Z0-9]*$")))
      }
    }

    s"not bind strings without ndrc prefix" in {
      forAll(stringsWithMaxLengthAlpha(maxLength) -> "invalidFormat") {
        string =>
          val result = form.bind(Map(fieldName -> (string))).apply(fieldName)
          result.errors shouldEqual Seq( FormError(fieldName, ndrcPrefixKey))
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
