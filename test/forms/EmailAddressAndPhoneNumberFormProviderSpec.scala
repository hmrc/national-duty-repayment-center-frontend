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

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError

class EmailAddressAndPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  val form = new EmailAddressAndPhoneNumberFormProvider()()

  ".email" must {
    val fieldName = "email"

    val basicEmail            = Gen.const("foo@example.com")
    val emailWithSpecialChars = Gen.const("aBcD.!#$%&'*+/=?^_`{|}~-123@foo-bar.example.com")
    val validData             = Gen.oneOf(basicEmail, emailWithSpecialChars)

    behave like fieldThatBindsValidData(form, fieldName, validData)

  }

  ".phone" must {

    val fieldName = "phone"
    val length    = 11

    "bind telephone number" in {

      val result = form.bind(Map(fieldName -> "01234567890", "value" -> "02"))(fieldName)
      result.value.get shouldBe "01234567890"
      result.errors shouldBe List.empty
    }

    "error if telephone number too short" in {

      val result = form.bind(Map(fieldName -> "0123456789", "value" -> "02"))(fieldName)
      result.errors shouldBe List(FormError(fieldName, "phoneNumber.error.length", Seq(length)))
    }

    "error if telephone number too long" in {

      val result = form.bind(Map(fieldName -> "0123456789", "value" -> "02"))(fieldName)
      result.errors shouldBe List(FormError(fieldName, "phoneNumber.error.length", Seq(length)))
    }

    "error if telephone number invalid" in {

      val result = form.bind(Map(fieldName -> "12345678901", "value" -> "02"))(fieldName)
      result.errors shouldBe List(FormError(fieldName, "phoneNumber.error.invalid", Seq(Validation.phoneNumberPattern)))
    }

  }
}
