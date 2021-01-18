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

class HowManyEntriesFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "howManyEntries.error.required"
  val lengthKey = "howManyEntries.error.length"
  val maxLength = 999999

  val form = new HowManyEntriesFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Validation.numberOfEntries
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = 999999,
      lengthError = FormError(fieldName, lengthKey, Seq(Validation.numberOfEntries))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
