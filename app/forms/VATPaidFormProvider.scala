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
        "ActualPaidAmount" -> decimal(
          "vatPaid.actualamountpaid.error.required",
          "vatPaid.actualamountpaid.error.notANumber"
        )
          .verifying(
            firstError(regexp(monetaryPattern, "vatPaid.actualamountpaid.error.decimalPlaces")),
            greaterThanZero("vatPaid.actualamountpaid.error.greaterThanZero")
          ).transform[BigDecimal](BigDecimal.apply, _.setScale(2).toString)
          .verifying(maximumValue[BigDecimal](99999999999.99, "vatPaid.actualamountpaid.error.length"))
          .transform[String](d => d.toString, i => i.toDouble),
        "ShouldHavePaidAmount" -> decimal(
          "vatPaid.shouldhavepaid.error.required",
          "vatPaid.shouldhavepaid.error.notANumber"
        )
          .verifying(
            firstError(regexp(monetaryPattern, "vatPaid.shouldhavepaid.error.decimalPlaces")),
            greaterThanOrEqualZero("vatPaid.shouldhavepaid.error.greaterThanZero")
          ).transform[BigDecimal](BigDecimal.apply, _.setScale(2).toString)
          .verifying(maximumValue[BigDecimal](99999999999.99, "vatPaid.actualamountpaid.error.length"))
          .transform[String](d => d.toString, i => i.toDouble)
      )(RepaymentAmounts.apply)(RepaymentAmounts.unapply)
        .verifying("vatPaid.amounts.error.same", vat => vat.dueAmount != 0)
        .verifying("vatPaid.amounts.error.greater", vat => vat.dueAmount >= 0)
    )

}
