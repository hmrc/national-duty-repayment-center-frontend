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
import models.EORI
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.{Form, FormError}

class EnterAgentEORIFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "enterAgentEORI.error.required"
  val lengthKey   = "enterAgentEORI.error.valid"

  val form: Form[EORI] = new EnterAgentEORIFormProvider()()

  ".value" must {

    val fieldName = "value"

    val validData = for {
      firstChars <- Gen.listOfN(2, "GB").map(_.mkString)
      numDigits  <- Gen.choose(1, 12)
    } yield s"$firstChars$numDigits"

    behave like fieldThatBindsValidData(form, fieldName, validData)

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

    "bind values with eori expression" in {

      val result = form.bind(Map(fieldName -> "GB123456123456")).apply(fieldName)
      result.value.get shouldBe "GB123456123456"
      result.errors shouldBe List.empty
    }

    "trim white spaces in EORI" in {
      val result = form.bind(Map(fieldName -> "GB 123456 123456"))
      result.get.value shouldBe "GB123456123456"
      result.errors shouldBe List.empty
    }
  }
}
