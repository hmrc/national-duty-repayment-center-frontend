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

package services

import connectors.NDRCConnector

import javax.inject.Inject
import models.{ClaimId, UserAnswers}
import models.requests.{AmendClaimRequest, CreateClaimRequest, DataRequest}
import models.responses.ClientClaimSuccessResponse
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Logger
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError

import scala.concurrent.{ExecutionContext, Future}
case class CaseAlreadyExists(msg: String) extends RuntimeException(msg)

class ClaimService @Inject()(
                              nDRCConnector: NDRCConnector
                            )(
                              implicit ec: ExecutionContext
                            ) {


  def submitClaim(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[String] = {
    val maybeRegistrationRequest: Option[CreateClaimRequest] = CreateClaimRequest.buildValidClaimRequest(userAnswers)

    maybeRegistrationRequest match {
      case Some(value) =>
         nDRCConnector.submitClaim(value).map { clientClaimResponse =>
        if (clientClaimResponse.result.map(_.caseId).nonEmpty)
          clientClaimResponse.result.map(_.caseId).get
        else
          clientClaimResponse.error match {
            case _ =>
              val message = clientClaimResponse.error.map(_.errorCode).map(_ + " ").getOrElse("") +
                clientClaimResponse.error.map(_.errorMessage).getOrElse("")
              Logger.error(s"Create claim submission failed due to $message")
              throw new RuntimeException(message)
          }
    }
      case None =>
        Logger.error("Unsuccessful claim submission, did not contain sufficient UserAnswers data to construct CreateClaimRequest")
        throw new RuntimeException("UserAnswers did not contain sufficient data to construct CreateClaimRequest")
    }
  }

  def submitAmendClaim(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[String] = {
    val maybeAmendRequest: Option[AmendClaimRequest] = AmendClaimRequest.buildValidAmendRequest(userAnswers)

    maybeAmendRequest match {
      case Some(value) =>
        nDRCConnector.submitAmendClaim(value).map { clientClaimResponse =>
          if (clientClaimResponse.result.map(_.caseId).nonEmpty)
            clientClaimResponse.result.map(_.caseId).get
          else
            clientClaimResponse.error match {
              case _ =>
                val message = clientClaimResponse.error.map(_.errorCode).map(_ + " ").getOrElse("") +
                  clientClaimResponse.error.map(_.errorMessage).getOrElse("")
                Logger.error(s"Amend claim submission failed due to $message")
                throw new RuntimeException(message)
            }
        }
      case None =>
        Logger.error("Unsuccessful amend claim submission, did not contain sufficient UserAnswers data to construct AmendClaimRequest")
        throw new RuntimeException("UserAnswers did not contain sufficient data to construct AmendClaimRequest")
    }
  }
}



