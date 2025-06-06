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

package models.bars

import models.bars.AssessBusinessBankDetailsResponse.{indeterminate, no, partial, yes}
import play.api.libs.json.{Json, OFormat}

case class AssessBusinessBankDetailsResponse(
  sortCodeIsPresentOnEISCD: String,
  accountNumberIsWellFormatted: String,
  nonStandardAccountDetailsRequiredForBacs: String,
  accountExists: String,
  nameMatches: String,
  sortCodeSupportsDirectCredit: String
  // accountName: Option[String] - Field which will contains account name if the name match is partial, not yet supported in NDRC
) {
  val sortcodeExists: Boolean               = sortCodeIsPresentOnEISCD == yes
  val accountNumberWellFormatted: Boolean   = Set(yes, indeterminate).contains(accountNumberIsWellFormatted)
  val sortcodeSupportsDirectCredit: Boolean = sortCodeSupportsDirectCredit == yes
  val rollNotRequired: Boolean              = nonStandardAccountDetailsRequiredForBacs == no
  val accountValid: Boolean                 = Set(yes, indeterminate).contains(accountExists)
  val nameValid: Boolean                    = Set(yes, indeterminate, partial).contains(nameMatches)
}

object AssessBusinessBankDetailsResponse {
  implicit val format: OFormat[AssessBusinessBankDetailsResponse] = Json.format[AssessBusinessBankDetailsResponse]

  private val yes           = "yes"
  private val no            = "no"
  private val indeterminate = "indeterminate"
  private val partial       = "partial"

}
