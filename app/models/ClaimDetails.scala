/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import java.time.LocalDate
import play.api.libs.json.{Json, OFormat}

final case class ClaimDetails(
                               formType: FormType,
                               customRegulationType: CustomRegulationType,
                               claimedUnderArticle: ClaimedUnderArticle,
                               claimant: Claimant,
                               claimType: ClaimType,
                               noOfEntries: Option[NoOfEntries],
                               epu: EPU,
                               entryNumber: EntryNumber,
                               entryDate: LocalDate,
                               claimReason: ClaimReason,
                               claimDescription: ClaimDescription,
                               dateReceived: LocalDate,
                               claimDate: LocalDate,
                               payeeIndicator: PayeeIndicator,
                               paymentMethod: PaymentMethod
                             )

object ClaimDetails {
  implicit val format: OFormat[ClaimDetails] = Json.format[ClaimDetails]
}