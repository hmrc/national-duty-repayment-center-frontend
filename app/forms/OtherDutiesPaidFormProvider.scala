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

import javax.inject.Inject
import forms.mappings.Mappings
import models.RepaymentAmounts
import play.api.data.Form
import play.api.data.Forms.mapping

class OtherDutiesPaidFormProvider @Inject() extends Mappings {

  def apply(): Form[RepaymentAmounts] = Form(
    mapping(
      "ActualPaidAmount" -> decimal("otherDutiesPaid.actualamountpaid.error.required",
        "otherDutiesPaid.actualamountpaid.error.notANumber")
        .verifying(
          firstError(
            regexp(Validation.monetaryPattern, "otherDutiesPaid.actualamountpaid.error.decimalPlaces"),
            greaterThanZero("otherDutiesPaid.actualamountpaid.error.greaterThanZero"),
            maximumValue("99999999999.99", "otherDutiesPaid.actualamountpaid.error.length")
          )
        ),
      "ShouldHavePaidAmount" -> decimal("otherDutiesPaid.shouldhavepaid.error.required",
        "otherDutiesPaid.shouldhavepaid.error.notANumber")
        .verifying(
          firstError(
            regexp(Validation.monetaryPattern, "otherDutiesPaid.shouldhavepaid.error.decimalPlaces"),
            greaterThanZero("otherDutiesPaid.shouldhavepaid.error.greaterThanZero"),
            maximumValue("99999999999.99", "otherDutiesPaid.shouldhavepaid.error.length")
          )
        )
    )(RepaymentAmounts.apply)(RepaymentAmounts.unapply)
      .verifying("otherDutiesPaid.amounts.error.same", duty => duty.dueAmount != 0)
      .verifying("otherDutiesPaid.amounts.error.greater", duty => duty.dueAmount >= 0)
  )
}
