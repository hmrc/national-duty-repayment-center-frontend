/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.mappings.Implicits.SanitizedString
import forms.mappings.Mappings
import models.BankDetails
import models.bars.BARSResult
import play.api.data.Forms._
import play.api.data.{Form, FormError}

import javax.inject.Inject

class BankDetailsFormProvider @Inject() extends Mappings {

  private val accountName   = "AccountName"
  private val sortCode      = "SortCode"
  private val accountNumber = "AccountNumber"

  def apply(): Form[BankDetails] = {

    val formToModel = (accountName: String, sortCode: String, accountNumber: String) =>
      BankDetails(accountName, sortCode.stripSpacesAndDashes, accountNumber.stripSpacesAndDashes.leftPadAccountNumber)

    Form(
      mapping(
        accountName -> text("bankDetails.name.error.required")
          .verifying(firstError(maxLength(40, "bankDetails.name.error.length"))),
        sortCode -> text("bankDetails.sortCode.error.required")
          .verifying(
            firstError(regexp(Validation.sortCodePattern, "bankDetails.sortCode.error.invalid", _.stripSpacesAndDashes))
          ),
        accountNumber -> text("bankDetails.accountNumber.error.required")
          .verifying(
            firstError(
              regexp(Validation.accountNumberPattern, "bankDetails.accountNumber.error.invalid", _.stripSpacesAndDashes)
            )
          )
      )(formToModel)(BankDetails.unapply)
    )
  }

  def processBarsResult(barsResult: BARSResult, bankDetails: BankDetails): Option[Form[BankDetails]] =
    if (barsResult.isValid)
      None
    else
      Some(apply().fill(bankDetails).copy(errors = barsResult match {

        case bars if !bars.sortcodeExists =>
          Seq(FormError(sortCode, "bankDetails.bars.validation.sortcodeNotFound"))

        case bars if !bars.validAccountAndSortCode =>
          Seq(FormError(accountNumber, "bankDetails.bars.validation.modCheckFailed"))

        case bars if !bars.sortcodeAcceptsDirectCredit =>
          Seq(FormError(sortCode, "bankDetails.bars.validation.bacsNotSupported"))

        case bars if !bars.rollNotRequired => Seq(FormError(sortCode, "bankDetails.bars.validation.rollRequired"))

        case bars if !bars.accountValid => Seq(FormError(accountNumber, "bankDetails.bars.validation.accountInvalid"))

        case bars if !bars.companyNameValid =>
          Seq(FormError(accountName, "bankDetails.bars.validation.companyNameInvalid"))

        case _ => Seq(FormError("", "bankDetails.bars.validation.failed"))
      }))

}
