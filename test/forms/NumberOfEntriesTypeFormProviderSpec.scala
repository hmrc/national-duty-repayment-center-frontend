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
import org.scalacheck.Gen
import play.api.data.FormError

class NumberOfEntriesTypeFormProviderSpec extends OptionFieldBehaviours with StringFieldBehaviours {

  val form = new NumberOfEntriesTypeFormProvider()()

  val requiredKey = "numberOfEntriesType.error.required"

  ".value" must {

    val fieldName = "value"
    val requiredKey = "numberOfEntriesType.error.required"

    behave like optionsField[Entries](
      form,
      fieldName,
      validValues  = Seq(Entries.apply(NumberOfEntriesType.Single,intsAboveValue(1).toString),
        Entries.apply(NumberOfEntriesType.Multiple,intsAboveValue(1).toString)),
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  /*".entries" must {

    val fieldName = "value"
    val requiredKey = "howManyEntries.error.required"
    val lengthKey = "howManyEntries.error.length"
    val maxLength = 999999

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Validation.numberOfEntries
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(Validation.numberOfEntries))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "fail to bind entries with characters" in {
      val result = form.bind(Map(fieldName -> "abcjf")).apply(fieldName)
      val expectedError = FormError(fieldName, lengthKey, Seq(Validation.numberOfEntries))
      result.errors shouldEqual Seq(expectedError)
    }

    "fail to bind a value" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      val expectedError = error(fieldName, requiredKey)

      result.errors shouldEqual(expectedError)
    }
  }*/
}
