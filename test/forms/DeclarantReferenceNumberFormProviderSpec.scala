/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{DeclarantReferenceNumber, DeclarantReferenceType}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError

class DeclarantReferenceNumberFormProviderSpec extends OptionFieldBehaviours with StringFieldBehaviours {

  val form = new DeclarantReferenceNumberFormProvider()()

  val radioFieldName = "value"
  val minLength      = 1
  val maxLength      = 50

  ".value" must {

    val requiredKey = "declarantReferenceNumber.error.required"

    behave like optionsField[DeclarantReferenceNumber](
      form,
      radioFieldName,
      validValues = Seq(
        DeclarantReferenceNumber.apply(DeclarantReferenceType.Yes, None),
        DeclarantReferenceNumber.apply(
          DeclarantReferenceType.No,
          Some(stringsWithMinAndMaxLength(minLength, maxLength).toString)
        )
      ),
      invalidError = FormError(radioFieldName, "error.invalid")
    )

    behave like mandatoryField(form, radioFieldName, requiredError = FormError(radioFieldName, requiredKey))
  }

  ".declarantReferenceNumber" must {

    val fieldName   = "declarantReferenceNumber"
    val requiredKey = "declarantReferenceNumber.error.required.declarantReferenceNumber"
    val maxLength   = 50
    val minLength   = 1

    behave like fieldThatBindsValidData(form, fieldName, stringsWithMinAndMaxLength(minLength, maxLength))

    behave like mandatoryField(
      form.bind(Map(radioFieldName -> "01")),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "fail to bind reference number above maxLength" in {
      val results = List(
        form.bind(Map(radioFieldName -> "01")).bind(Map(fieldName -> "1")).apply(fieldName),
        form.bind(Map(radioFieldName -> "01")).bind(Map(fieldName -> (maxLength + 1).toString)).apply(fieldName)
      )
      val expectedError = FormError(fieldName, requiredKey, Seq())
      results.foreach {
        result =>
          result.errors shouldEqual Seq(expectedError)
      }
    }

    "fail to bind a value" in {
      val result        = form.bind(Map(radioFieldName -> "01")).bind(Map(fieldName -> "")).apply(fieldName)
      val expectedError = error(fieldName, requiredKey)

      result.errors shouldEqual expectedError
    }
  }
}
