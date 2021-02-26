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

import forms.behaviours.{DateBehaviours, StringFieldBehaviours}
import models.EntryDetails
import play.api.data.FormError

import java.time.LocalDate

class EntryDetailsFormProviderSpec extends StringFieldBehaviours with DateBehaviours {

  val form = new EntryDetailsFormProvider()()
  def buildFormData(epu: Option[String] = Some("123"),
                    entryNumber: Option[String] = Some("123456Q"),
                    day: Option[String] = Some("31"),
                    month: Option[String] = Some("12"),
                    year: Option[String] = Some("2020")): Map[String, String] =
    (
      epu.map(_ => "EPU" -> epu.get) ++
        entryNumber.map(_ => "EntryNumber" -> entryNumber.get) ++
        day.map(_ => "EntryDate.day" -> day.get) ++
        month.map(_ => "EntryDate.month" -> month.get) ++
        year.map(_ => "EntryDate.year" -> year.get)
      ).toMap

  ".EPU" must {

    val fieldName = "EPU"
    val requiredKey = "entryDetails.claimEpu.error.required"
    val lengthKey = "entryDetails.claimEpu.error.valid"
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


  ".EntryNumber" must {

    val fieldName = "EntryNumber"
    val requiredKey = "entryDetails.entryNumber.error.required"
    val lengthKey = "entryDetails.entryNumber.error.valid"
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
    val lengthKey = "entryDetails.claimEpu.error.valid"

    val result = form.bind(Map(fieldName -> "1234")).apply(fieldName)
    val expectedError = FormError(fieldName, lengthKey, Seq(Validation.epu))
    result.errors shouldEqual Seq(expectedError)
  }


  "fail to bind entries that do not contain 6 digits and a letter" in {
    val fieldName = "EntryNumber"
    val lengthKey = "entryDetails.entryNumber.error.valid"

    val result = form.bind(Map(fieldName -> "12345678AQ")).apply(fieldName)
    val expectedError = FormError(fieldName, lengthKey, Seq(Validation.epuEntryNumber))
    result.errors shouldEqual Seq(expectedError)
  }

  "Accept valid form  data" in {
    val form2 = new EntryDetailsFormProvider().apply().bind(
      buildFormData())

    form2.hasErrors shouldBe  false

  }

  "Accept valid form date data" in {
    val form2 = new EntryDetailsFormProvider().apply().bind(
      buildFormData())

    form2.value shouldBe  Some(EntryDetails("123", "123456Q", LocalDate.of(2020, 12, 31)))

  }

  "Fail if the Date has a future day" in {
    val form2 = new EntryDetailsFormProvider().apply().bind(
      buildFormData(
        day = Some(s"${LocalDate.now().getDayOfMonth + 1}"),
        month = Some(s"${LocalDate.now().getMonthValue}"),
        year = Some(s"${LocalDate.now().getYear}")
      ))

    form2.errors.size shouldBe 1
    form2.errors.head.key shouldBe "EntryDate"
    form2.errors.head.message shouldBe "entryDetails.claimEntryDate.error.invalid"

  }

  "Fail if the Date contains an error in the Year" in {
    val form2 = new EntryDetailsFormProvider().apply().bind(buildFormData(year = Some("20")))

    form2.errors.size shouldBe  1
    form2.errors.head.key shouldBe  "EntryDate"
    form2.errors.head.message shouldBe  "entryDetails.claimEntryDate.error.invalid"
  }


  "Fail if the Date contains an error in the day" in {
    val form2 = new EntryDetailsFormProvider().apply().bind(buildFormData(month = Some("43")))

    form2.errors.size shouldBe  1
    form2.errors.head.key shouldBe  "EntryDate"
    form2.errors.head.message shouldBe  "entryDetails.claimEntryDate.error.invalid"
  }
}