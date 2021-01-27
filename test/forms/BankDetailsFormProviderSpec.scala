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
import org.scalacheck.Gen
import play.api.data.FormError

class BankDetailsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "bankDetails.error.required"
  val lengthKey = "bankDetails.error.length"

  val form = new BankDetailsFormProvider()()

  ".AccountName" must {

    val fieldName = "AccountName"
    val requiredKey = "bankDetails.name.error.required"
    val lengthKey = "bankDetails.name.error.length"
    val invalidKey = "bankDetails.name.error.invalid"
    val maxLength = 40

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputsWithMaxLength(maxLength)
    )

    behave like fieldThatPreventsUnsafeInput(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern))
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

  ".SortCode" must {

    val fieldName = "SortCode"
    val requiredKey = "bankDetails.sortCode.error.required"
    val invalidKey = "bankDetails.sortCode.error.invalid"

    val validSortCodeGen = for {
      firstDigits     <- Gen.listOfN(2, Gen.numChar).map(_.mkString)
      firstSeparator  <- Gen.oneOf(' ', '-').map(_.toString)
      secondDigits    <- Gen.listOfN(2, Gen.numChar).map(_.mkString)
      secondSeparator <- Gen.oneOf(' ', '-').map(_.toString)
      thirdDigits     <- Gen.listOfN(2, Gen.numChar).map(_.mkString)
    } yield s"$firstDigits$firstSeparator$secondDigits$secondSeparator$thirdDigits"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validSortCodeGen
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "bind sort codes in nnnnnn format" in {
      val result = form.bind(Map(fieldName -> "123456")).apply(fieldName)
      result.value.value shouldBe "123456"
    }

    "bind sort codes in nn-nn-nn format" in {
      val result = form.bind(Map(fieldName -> "12-34-56")).apply(fieldName)
      result.value.value shouldBe "12-34-56"
    }

    "bind sort codes in nn nn nn format" in {
      val result = form.bind(Map(fieldName -> "12 34 56")).apply(fieldName)
      result.value.value shouldBe "12 34 56"
    }

    "bind sort codes in nn   nn    nn format" in {
      val result = form.bind(Map(fieldName -> "12   34   56")).apply(fieldName)
      result.value.value shouldBe "12   34   56"
    }

    "not bind sort codes with characters" in {
      val result = form.bind(Map(fieldName -> "abcdef")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.sortCodePattern.toString))
      result.errors shouldEqual Seq(expectedError)
    }

    "not bind sort codes with less than 6 digit" in {
      val result = form.bind(Map(fieldName -> "12   34  5")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.sortCodePattern.toString))
      result.errors shouldEqual Seq(expectedError)
    }

    "not bind sort codes with more than 6 digit" in {
      val result = form.bind(Map(fieldName -> "12   34  5678")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.sortCodePattern.toString))
      result.errors shouldEqual Seq(expectedError)
    }
  }

  ".AccountNumber" must {

    val fieldName = "AccountNumber"
    val requiredKey = "bankDetails.accountNumber.error.required"
    val invalidKey = "bankDetails.accountNumber.error.invalid"
    val lengthKey = "bankDetails.accountNumber.error.length"
    val minLength = 6
    val maxLength = 8

    val validAccountNumberGen = for {
      length <- Gen.choose(minLength, maxLength)
      digits <- Gen.listOfN(length, Gen.numChar)
    } yield digits.mkString

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validAccountNumberGen
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

    "bind account number in format with any number of spaces nn   nn    nn format" in {
      val result = form.bind(Map(fieldName -> "12   34   56")).apply(fieldName)
      result.value.value shouldBe "12   34   56"
    }

    "not bind strings with characters" in {
      val result = form.bind(Map(fieldName -> "abcdef")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.accountNumberPattern.toString))
      result.errors shouldEqual Seq(expectedError)
    }
  }
}
