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

import forms.behaviours.{OptionFieldBehaviours, StringFieldBehaviours}
import models.{Entries, NumberOfEntriesType}
import play.api.data.FormError

class NumberOfEntriesTypeFormProviderSpec extends OptionFieldBehaviours with StringFieldBehaviours {

  val form = new NumberOfEntriesTypeFormProvider()()

  def buildFormDataWithSpaces(
    value: Option[String] = Some("02"),
    entries: Option[String] = Some(" 1 2 ")
  ): Map[String, String] =
    (
      value.map(_ => "value" -> value.get) ++
        entries.map(_ => "entries" -> entries.get)
    ).toMap

  val requiredKey    = "numberOfEntriesType.error.required"
  val radioFieldName = "value"

  ".value" must {

    val requiredKey = "numberOfEntriesType.error.required"

    behave like optionsField[Entries](
      form,
      radioFieldName,
      validValues = Seq(
        Entries.apply(NumberOfEntriesType.Single, None),
        Entries.apply(NumberOfEntriesType.Multiple, Some(intsAboveValue(1).toString))
      ),
      invalidError = FormError(radioFieldName, "error.invalid")
    )

    behave like mandatoryField(form, radioFieldName, requiredError = FormError(radioFieldName, requiredKey))
  }

  ".entries" must {

    val fieldName   = "entries"
    val requiredKey = "howManyEntries.error.required"
    val maxLength   = 999999

    behave like fieldThatBindsValidData(form, fieldName, Validation.numberOfEntries)

    behave like mandatoryField(
      form.bind(Map(radioFieldName -> "02")),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "fail to bind entries with characters" in {
      val results = List(
        form.bind(Map(radioFieldName -> "02")).bind(Map(fieldName -> "1")).apply(fieldName),
        form.bind(Map(radioFieldName -> "02")).bind(Map(fieldName -> "abcjf")).apply(fieldName),
        form.bind(Map(radioFieldName -> "02")).bind(Map(fieldName -> (maxLength + 1).toString)).apply(fieldName)
      )
      val expectedError = FormError(fieldName, requiredKey, Seq())
      results.foreach {
        result =>
          result.errors shouldEqual Seq(expectedError)
      }
    }

    "fail to bind a value" in {
      val result        = form.bind(Map(radioFieldName -> "02")).bind(Map(fieldName -> "")).apply(fieldName)
      val expectedError = error(fieldName, requiredKey)

      result.errors shouldEqual expectedError
    }

    "trim white spaces in number of entries" in {
      val result = new NumberOfEntriesTypeFormProvider().apply().bind(buildFormDataWithSpaces())
      result.get shouldBe Entries(NumberOfEntriesType.Multiple, Some("12"))
      result.errors shouldBe List.empty
    }
  }
}
