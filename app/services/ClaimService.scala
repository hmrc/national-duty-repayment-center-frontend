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

package services

import connectors.NDRCConnector
import javax.inject.Inject
import models.{ClaimId, UserAnswers}
import models.requests.{CreateClaimRequest, DataRequest}
import models.responses.ClientClaimSuccessResponse
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class ClaimService @Inject()(
                              nDRCConnector: NDRCConnector
                            )(
                              implicit ec: ExecutionContext
                            ) {


  def submitClaim(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[String] = {
    val maybeRegistrationRequest: Option[CreateClaimRequest] = CreateClaimRequest.buildValidClaimRequest(userAnswers)
    println("XXXXXXXXXXX submitClaim maybeRegistrationRequest " + maybeRegistrationRequest)

    maybeRegistrationRequest match {
      case Some(value) =>
        for {
          claimId: ClientClaimSuccessResponse <- nDRCConnector.submitClaim(value)
        } yield {
          //val _ = auditService.audit(buildAuditModel(value, registration, request))
          claimId.result.get
        }
      case None =>
        Logger.error("Unsuccessful claim submission, did not contain sufficient UserAnswers data to construct CreateClaimRequest")
        throw new RuntimeException("UserAnswers did not contain sufficient data to construct CreateClaimRequest")
    }
  }
}

