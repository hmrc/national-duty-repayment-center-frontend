/*
 * Copyright 2025 HM Revenue & Customs
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

import java.time.LocalDate
import formats.Format

import javax.inject.Inject
import forms.mappings.Mappings
import models.EntryDetails
import play.api.data.Form
import play.api.data.Forms._

class EntryDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[EntryDetails] = {

    val minDateLimit = LocalDate.parse("1900-01-01")

    Form(
      mapping(
        "EPU" -> textNoSpaces("entryDetails.claimEpu.error.required")
          .verifying(regexp(Validation.epu, "entryDetails.claimEpu.error.valid")),
        "EntryNumber" -> textNoSpaces("entryDetails.entryNumber.error.required")
          .verifying(regexp(Validation.epuEntryNumber, "entryDetails.entryNumber.error.valid")),
        "EntryDate" -> localDate(
          invalidKey = "entryDetails.claimEntryDate.error.invalid",
          requiredKey = "entryDetails.claimEntryDate.error.required"
        )
          .verifying(maxDateToday("entryDetails.claimEntryDate.error.invalid_future"))
          .verifying(
            minDate(minDateLimit, "entryDetails.claimEntryDate.error.invalid_past", Format.formattedDate(minDateLimit))
          )
      )(EntryDetails.apply)(EntryDetails.unapply)
    )
  }

}
