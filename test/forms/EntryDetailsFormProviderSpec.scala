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

import java.time.LocalDate

import formats.Format
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
      Validation.epu
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(Validation.epu))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".value" must  {
    val fieldName = "EntryDate"
    val date = LocalDate.now.minusDays(1)

    val validData = datesBetween(
      min = LocalDate.of(1900, 1, 1),
      max = LocalDate.now.minusDays(1)
    )

    behave like dateField(form, fieldName, validData)

    behave like dateFieldWithMax(
      form,
      fieldName,
      LocalDate.now.minusDays(1),
      FormError(fieldName, "entryDetails.claimEntryDate.error.invalid", Seq(Format.formattedDate(date)))
    )

    behave like mandatoryDateField(form, "value", "entryDetails.claimEntryDate.error.required.all")

    val invalidMinYear = LocalDate.parse("0900-01-01")

    behave like dateFieldWithMin(
      form,
      fieldName,
      invalidMinYear,
      FormError(fieldName, "entryDetails.claimEntryDate.error.invalid", Seq(Format.formattedDate(invalidMinYear)))
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
      lengthError = FormError(fieldName, lengthKey, Seq(Validation.epuEntryNumber))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }


  "fail to bind entries that are not 3 digits" in {
    val fieldName = "EPU"
    val lengthKey = "entryDetails.claimEpu.error.length"

    val result = form.bind(Map(fieldName -> "1234")).apply(fieldName)
    val expectedError = FormError(fieldName, lengthKey, Seq(Validation.epu))
    result.errors shouldEqual Seq(expectedError)
  }


  "fail to bind entries that do not contain 6 digits and a letter" in {
    val fieldName = "EntryNumber"
    val lengthKey = "entryDetails.entryNumber.error.length"

    val result = form.bind(Map(fieldName -> "12345678AQ")).apply(fieldName)
    val expectedError = FormError(fieldName, lengthKey, Seq(Validation.epuEntryNumber))
    result.errors shouldEqual Seq(expectedError)
  }




}
