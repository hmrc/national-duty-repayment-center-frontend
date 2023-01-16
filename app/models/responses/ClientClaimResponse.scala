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

package models.responses

import models.responses.ClientClaimResponse.{closedCaseRegex, invalidCaseRegex}
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError

final case class ClientClaimResponse(correlationId: String, caseId: Option[String], error: Option[ApiError] = None) {
  val isSuccess    = error.isEmpty
  val isNotFound   = error.flatMap(_.errorMessage).exists(_.matches(invalidCaseRegex))
  val isCaseClosed = error.flatMap(_.errorMessage).exists(_.matches(closedCaseRegex))
  val isKnownError = isNotFound || isCaseClosed
}

object ClientClaimResponse {

  val invalidCaseRegex = ".*03 ?- ?Invalid Case ID.*"
  val closedCaseRegex  = ".*04 ?- ?Requested case already closed.*"

  implicit val format: OFormat[ClientClaimResponse] = Json.format[ClientClaimResponse]

  implicit val optionFormat: Format[Option[ClientClaimResponse]] =
    Format.optionWithNull[ClientClaimResponse]

}
