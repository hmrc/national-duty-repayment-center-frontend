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
import models.UserAnswers
import models.requests.{AmendClaimBuilder, AmendClaimRequest, CreateClaimBuilder, CreateClaimRequest, DataRequest}
import uk.gov.hmrc.http.HeaderCarrier
import java.util.UUID

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
case class CaseAlreadyExists(msg: String) extends RuntimeException(msg)

class ClaimService @Inject() (
  connector: NDRCConnector,
  createClaimBuilder: CreateClaimBuilder,
  amendClaimBuilder: AmendClaimBuilder
)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def submitClaim(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[String] = {
    val maybeRegistrationRequest: Option[CreateClaimRequest] = createClaimBuilder.buildValidClaimRequest(userAnswers)

    maybeRegistrationRequest match {
      case Some(value) =>
        connector.submitClaim(value, correlationId(hc)).map { clientClaimResponse =>
          clientClaimResponse.caseId match {
            case Some(value) => value
            case None =>
              val message = clientClaimResponse.error.map(_.errorCode).map(_ + " ").getOrElse("") +
                clientClaimResponse.error.map(_.errorMessage).getOrElse("")
              logger.error(s"Create claim submission failed due to $message")
              throw new RuntimeException(message)
          }
        }
      case None =>
        logger.error(
          "Unsuccessful claim submission, did not contain sufficient UserAnswers data to construct CreateClaimRequest"
        )
        throw new RuntimeException("UserAnswers did not contain sufficient data to construct CreateClaimRequest")
    }
  }

  def submitAmendClaim(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[String] = {
    val maybeAmendRequest: Option[AmendClaimRequest] = amendClaimBuilder.buildValidAmendRequest(userAnswers)

    maybeAmendRequest match {
      case Some(value) =>
        connector.submitAmendClaim(value, correlationId(hc)).map { clientClaimResponse =>
          if (clientClaimResponse.caseId.nonEmpty)
            clientClaimResponse.caseId.get
          else
            clientClaimResponse.error match {
              case _ =>
                val message = clientClaimResponse.error.map(_.errorCode).map(_ + " ").getOrElse("") +
                  clientClaimResponse.error.map(_.errorMessage).getOrElse("")
                logger.error(s"Amend claim submission failed due to $message")
                throw new RuntimeException(message)
            }
        }
      case None =>
        logger.error(
          "Unsuccessful amend claim submission, did not contain sufficient UserAnswers data to construct AmendClaimRequest"
        )
        throw new RuntimeException("UserAnswers did not contain sufficient data to construct AmendClaimRequest")
    }
  }

  private def correlationId(hc: HeaderCarrier): String =
    hc.requestId.map(_.value).getOrElse(UUID.randomUUID().toString)

}
