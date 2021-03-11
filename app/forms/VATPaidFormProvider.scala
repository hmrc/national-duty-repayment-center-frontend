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

import forms.Validation.monetaryPattern
import javax.inject.Inject
import forms.mappings.Mappings
import models.RepaymentAmounts
import play.api.data.Form
import play.api.data.Forms.mapping

class VATPaidFormProvider @Inject() extends Mappings {

  def apply(): Form[RepaymentAmounts] =
    Form(
      mapping(
        "ActualPaidAmount" -> decimal("vatPaid.VATActuallyPaid.error.required",
          "vatPaid.VATActuallyPaid.error.notANumber")
          .verifying(
            firstError(
              regexp(monetaryPattern, "vatPaid.VATActuallyPaid.error.decimalPlaces")),
            greaterThanZero("vatPaid.VATActuallyPaid.error.greaterThanZero"),
            maximumValue("99999999999.99", "vatPaid.VATActuallyPaid.error.length"
            )
          ),
        "ShouldHavePaidAmount" -> decimal("vatPaid.VATShouldHavePaid.error.required",
          "vatPaid.VATShouldHavePaid.error.notANumber")
          .verifying(
            firstError(
              regexp(monetaryPattern, "vatPaid.VATShouldHavePaid.error.decimalPlaces")) ,
            greaterThanZero("vatPaid.VATShouldHavePaid.error.greaterThanZero"),
            maximumValue("99999999999.99", "vatPaid.VATShouldHavePaid.error.length")

          )
      )(RepaymentAmounts.apply)(RepaymentAmounts.unapply)
        .verifying("vatPaid.amounts.error.same", vat => vat.dueAmount != 0)
        .verifying("vatDue.amounts.error.greater", vat => vat.dueAmount >= 0)
    )
}