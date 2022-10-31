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

import data.BarsTestData
import forms.behaviours.StringFieldBehaviours
import models.BankDetails
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.FormError

class BankDetailsFormProviderSpec extends StringFieldBehaviours with BarsTestData {

  val requiredKey = "bankDetails.error.required"
  val lengthKey   = "bankDetails.error.length"

  val provider     = new BankDetailsFormProvider
  private val form = provider.apply()

  val accountNameField   = "AccountName"
  val sortCodeField      = "SortCode"
  val accountNumberField = "AccountNumber"

  ".AccountName" must {

    val fieldName   = accountNameField
    val requiredKey = "bankDetails.name.error.required"
    val lengthKey   = "bankDetails.name.error.length"

    val maxLength = 40
    behave like fieldThatBindsValidData(form, fieldName, safeInputsWithMaxLength(maxLength))

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))
  }

  ".SortCode" must {

    val fieldName   = sortCodeField
    val requiredKey = "bankDetails.sortCode.error.required"
    val invalidKey  = "bankDetails.sortCode.error.invalid"

    val validSortCodeGen = for {
      firstDigits     <- Gen.listOfN(2, Gen.numChar).map(_.mkString)
      firstSeparator  <- Gen.oneOf(' ', '-').map(_.toString)
      secondDigits    <- Gen.listOfN(2, Gen.numChar).map(_.mkString)
      secondSeparator <- Gen.oneOf(' ', '-').map(_.toString)
      thirdDigits     <- Gen.listOfN(2, Gen.numChar).map(_.mkString)
    } yield s"$firstDigits$firstSeparator$secondDigits$secondSeparator$thirdDigits"

    behave like fieldThatBindsValidData(form, fieldName, validSortCodeGen)

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

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
      val result        = form.bind(Map(fieldName -> "abcdef")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.sortCodePattern))
      result.errors shouldEqual Seq(expectedError)
    }

    "not bind sort codes with less than 6 digit" in {
      val result        = form.bind(Map(fieldName -> "12   34  5")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.sortCodePattern))
      result.errors shouldEqual Seq(expectedError)
    }

    "not bind sort codes with more than 6 digit" in {
      val result        = form.bind(Map(fieldName -> "12   34  5678")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.sortCodePattern))
      result.errors shouldEqual Seq(expectedError)
    }
  }

  ".AccountNumber" must {

    val fieldName   = accountNumberField
    val requiredKey = "bankDetails.accountNumber.error.required"
    val invalidKey  = "bankDetails.accountNumber.error.invalid"
    val minLength   = 6
    val maxLength   = 8

    val validAccountNumberGen = for {
      length <- Gen.choose(minLength, maxLength)
      digits <- Gen.listOfN(length, Gen.numChar)
    } yield digits.mkString

    behave like fieldThatBindsValidData(form, fieldName, validAccountNumberGen)

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, requiredKey))

    "bind account number in format with any number of spaces nn   nn    nn format" in {
      val result = form.bind(Map(fieldName -> "12   34   56")).apply(fieldName)
      result.value.value shouldBe "12   34   56"
    }

    "not bind strings with characters" in {
      val result        = form.bind(Map(fieldName -> "abcdef")).apply(fieldName)
      val expectedError = FormError(fieldName, invalidKey, Seq(Validation.accountNumberPattern))
      result.errors shouldEqual Seq(expectedError)
    }

    "not bind strings with less than 6 digit" in {
      val result = form.bind(Map(fieldName -> "12345")).apply(fieldName)

      result.errors shouldEqual Seq(FormError(fieldName, invalidKey, Seq(Validation.accountNumberPattern)))
    }

    "not bind strings with more than 8 digit" in {
      val result = form.bind(Map(fieldName -> "123456789")).apply(fieldName)

      result.errors shouldEqual Seq(FormError(fieldName, invalidKey, Seq(Validation.accountNumberPattern)))
    }
  }

  "Form" must {
    "Accept valid form data" in {
      val form = new BankDetailsFormProvider().apply().bind(buildFormData("123456", "12345678"))

      form.hasErrors shouldEqual false
      form.value shouldEqual Some(BankDetails("AccountName", "123456", "12345678"))
    }

    "Pad 6 digit account codes" in {
      val form = new BankDetailsFormProvider().apply().bind(buildFormData("123456", "123456"))

      form.hasErrors shouldEqual false
      form.value shouldEqual Some(BankDetails("AccountName", "123456", "00123456"))
    }

    "Pad 7 digit account codes" in {
      val form = new BankDetailsFormProvider().apply().bind(buildFormData("123456", "1234567"))

      form.hasErrors shouldEqual false
      form.value shouldEqual Some(BankDetails("AccountName", "123456", "01234567"))
    }

    "Pad 7 digit account code, when spaces and hyphens present" in {
      val form = new BankDetailsFormProvider().apply().bind(buildFormData("123456", "12 34 5-6-7"))

      form.hasErrors shouldEqual false
      form.value shouldEqual Some(BankDetails("AccountName", "123456", "01234567"))
    }

    "Remove dashes from sort codes" in {
      val form = new BankDetailsFormProvider().apply().bind(buildFormData("12-34-56", "12345678"))

      form.hasErrors shouldEqual false
      form.value shouldEqual Some(BankDetails("AccountName", "123456", "12345678"))
    }

    "Remove spaces from sort codes and account numbers" in {
      val form = new BankDetailsFormProvider().apply().bind(buildFormData(" 12 34 56 ", " 1234 5678 "))

      form.hasErrors shouldEqual false
      form.value shouldEqual Some(BankDetails("AccountName", "123456", "12345678"))
    }
  }

  "processBarsResult" must {

    val bankDetails = BankDetails("Name", "123456", "12345678")

    "return nothing when BARS check is successful" in {
      provider.processBarsResult(barsSuccessResult, bankDetails) shouldBe None
    }

    "return form with error when sort code does not exist" in {
      provider.processBarsResult(barsSortcodeDoesNotExistResult, bankDetails).map(_.errors).get shouldEqual Seq(
        FormError(sortCodeField, "bankDetails.bars.validation.sortcodeNotFound", Seq.empty)
      )
    }

    "return form with error when BACS not supported" in {
      provider.processBarsResult(barsBacsNotSupportedResult, bankDetails).map(_.errors).get shouldEqual Seq(
        FormError(sortCodeField, "bankDetails.bars.validation.bacsNotSupported", Seq.empty)
      )
    }

    "return form with error when account invalid" in {
      provider.processBarsResult(barsInvalidAccountResult, bankDetails).map(_.errors).get shouldEqual Seq(
        FormError(accountNumberField, "bankDetails.bars.validation.modCheckFailed", Seq.empty)
      )
    }

    "return form with error when roll number required" in {
      provider.processBarsResult(barsRollRequiredResult, bankDetails).map(_.errors).get shouldEqual Seq(
        FormError(sortCodeField, "bankDetails.bars.validation.rollRequired", Seq.empty)
      )
    }

    "return form with error when account name does not match" in {
      provider.processBarsResult(barsCompanyNameDoesNotMatchResult, bankDetails).map(_.errors).get shouldEqual Seq(
        FormError(accountNameField, "bankDetails.bars.validation.companyNameInvalid", Seq.empty)
      )
    }
  }

  private def buildFormData(sortCode: String, accountNumber: String) =
    Map(accountNameField -> "AccountName", sortCodeField -> sortCode, accountNumberField -> accountNumber)

}
