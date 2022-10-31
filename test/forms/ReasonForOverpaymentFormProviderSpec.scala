/*
 * Copyright 2022 HM Revenue & Customs
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
import models.ClaimDescription
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError

class ReasonForOverpaymentFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "reasonForOverpayment.error.required"
  val lengthKey   = "reasonForOverpayment.error.length"
  val maxLength   = 1200

  val form = new ReasonForOverpaymentFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(form, fieldName, stringsWithMaxLength(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))
    behave like mandatoryNotSpaceField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

    "remove carriage returns from submitted value" in {
      form.bind(Map("value" -> "Line1\r\nLine2")).value shouldBe Some(ClaimDescription("Line1\nLine2"))
    }

  }
}
