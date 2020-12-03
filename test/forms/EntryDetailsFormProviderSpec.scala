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

import java.time.LocalDate
import forms.behaviours.{DateBehaviours, StringFieldBehaviours}
import play.api.data.FormError

class EntryDetailsFormProviderSpec extends StringFieldBehaviours with DateBehaviours {

  val form = new EntryDetailsFormProvider()()

  ".EPU" must {

    val fieldName = "EPU"
    val requiredKey = "entryDetails.claimEpu.error.required"
    val lengthKey = "entryDetails.claimEpu.error.length"
    val maxLength = 3

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
  }

  ".EntryNumber" must {

    val fieldName = "EntryNumber"
    val requiredKey = "entryDetails.entryNumber.error.required"
    val lengthKey = "entryDetails.entryNumber.error.length"
    val maxLength = 7

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
  }

}
