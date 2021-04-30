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
import models.BankDetails
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._

class BankDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[BankDetails] = Form(
    mapping(
      "AccountName" -> text("bankDetails.name.error.required")
        .verifying(firstError(
          maxLength(40, "bankDetails.name.error.length"),
          regexp(Validation.safeInputPattern, "bankDetails.name.error.invalid")
        )),
      "SortCode" -> textNoSpaces("bankDetails.sortCode.error.required")
        .verifying(firstError(
          regexp(Validation.sortCodePattern.toString, "bankDetails.sortCode.error.invalid")
        )),
      "AccountNumber" -> textNoSpaces("bankDetails.accountNumber.error.required")
        .verifying(firstError(
          minLength(6, "bankDetails.accountNumber.error.length"),
          maxLength(8, "bankDetails.accountNumber.error.length"),
          regexp(Validation.accountNumberPattern.toString, "bankDetails.accountNumber.error.invalid")
        ))
    )(BankDetails.apply)(BankDetails.unapply)
  )
}
