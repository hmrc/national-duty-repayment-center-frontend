/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package services

import config.Constants
import connectors.{NDRCConnector, ParticipantRegistrationConnector}
import javax.inject.Inject
import models.audit.RegisterAuditModel
import models.requests.{DataRequest, RegistrationRequest}
import models.results.RegistrationResponse
import models.{BankInfo, CaseId, InternalId, RegistrationId, RestaurantInfo, TaxIdentifiers, UserAnswers}
import pages._
import play.api.Logger
import queries.AllRestaurantsQuery
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ClaimService @Inject()(
                              nDRCConnector: NDRCConnector
                            )(
                              implicit ec: ExecutionContext
                            ) {


  def submitClaim(internalId: InternalId, userAnswers: UserAnswers)(implicit hc: HeaderCarrier,
                                                                           request: DataRequest[_]): Future[CaseId] = {
    val maybeRegistrationRequest = RegistrationRequest.buildValidRegistrationRequest(internalId, userAnswers)

    maybeRegistrationRequest match {
      case Some(value) =>
        for {
          caseId <- nDRCConnector.submitClaim(value)
        } yield {
          //val _ = auditService.audit(buildAuditModel(value, registration, request))
          registration.id
        }
      case None =>
        Logger.error("Unsuccessful claim submission, did not contain sufficient UserAnswers data to construct CreateClaimRequest")
        throw new RuntimeException("UserAnswers did not contain sufficient data to construct CreateClaimRequest")
    }
  }
}

