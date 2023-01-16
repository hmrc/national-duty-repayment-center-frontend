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

import forms.behaviours.DecimalFieldBehaviours
import models.RepaymentAmounts
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.{Form, FormError}

class OtherDutiesPaidFormProviderSpec extends DecimalFieldBehaviours {

  val actualAmountPaidRequiredKey = "otherDutiesPaid.actualamountpaid.error.required"
  val shouldHavePaidRequiredKey   = "otherDutiesPaid.shouldhavepaid.error.required"
  val maxLength                   = 14
  val minimum                     = 0.00
  var maximum                     = 99999999999.99

  val validDataGenerator = decimalInRangeWithCommas(minimum.toDouble, maximum)

  def buildFormDataWithSpaces(
    ActualPaidAmount: Option[String] = Some(" 2 2 2 "),
    ShouldHavePaidAmount: Option[String] = Some(" 1 1 1 ")
  ): Map[String, String] =
    (
      ActualPaidAmount.map(_ => "ActualPaidAmount" -> ActualPaidAmount.get) ++
        ShouldHavePaidAmount.map(_ => "ShouldHavePaidAmount" -> ShouldHavePaidAmount.get)
    ).toMap

  val form: Form[RepaymentAmounts] = new OtherDutiesPaidFormProvider()()

  ".ActualPaidAmount" must {

    val fieldName = "ActualPaidAmount"

    behave like fieldThatBindsValidData(form, fieldName, validDataGenerator)

    behave like decimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "otherDutiesPaid.actualamountpaid.error.notANumber")
    )

    behave like decimalFieldWithMinimum(
      form,
      fieldName,
      minimum,
      expectedError = FormError(fieldName, "otherDutiesPaid.actualamountpaid.error.greaterThanZero")
    )

    "not bind decimals with 3 decimal place" in {
      val result = form.bind(Map(fieldName -> "1.111"))(fieldName)
      result.errors shouldEqual Seq(
        FormError(
          fieldName,
          "otherDutiesPaid.actualamountpaid.error.decimalPlaces",
          List(forms.Validation.monetaryPattern)
        )
      )
    }

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, actualAmountPaidRequiredKey))
  }

  ".ShouldHavePaidAmount" must {

    val fieldName = "ShouldHavePaidAmount"

    behave like fieldThatBindsValidData(form, fieldName, validDataGenerator)

    behave like decimalField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "otherDutiesPaid.shouldhavepaid.error.notANumber")
    )

    behave like decimalFieldWithMinimum(
      form,
      fieldName,
      -0.01,
      expectedError = FormError(fieldName, "otherDutiesPaid.shouldhavepaid.error.greaterThanZero")
    )

    "not bind decimals with 3 decimal place" in {
      val result = form.bind(Map(fieldName -> "1.111"))(fieldName)
      result.errors shouldEqual Seq(
        FormError(
          fieldName,
          "otherDutiesPaid.shouldhavepaid.error.decimalPlaces",
          List(forms.Validation.monetaryPattern)
        )
      )
    }

    behave like mandatoryField(form, fieldName, requiredError = FormError(fieldName, shouldHavePaidRequiredKey))
  }

  "trim white spaces in OtherDuties Amounts" in {
    val result = new OtherDutiesPaidFormProvider().apply().bind(buildFormDataWithSpaces())
    result.get shouldBe RepaymentAmounts("222", "111")
    result.errors shouldBe List.empty
  }
}
