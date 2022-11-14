/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.EORI
import models.requests.{Identification, IdentifierRequest}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    val eoriIdentifier = "EORINumber"

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(allEnrolments) {
      case allUsersEnrolments =>
        def eori: Option[EORI] =
          if (config.eoriIntegration.enabled)
            Some(
              allUsersEnrolments.getEnrolment(config.eoriIntegration.enrolmentKey).flatMap(enrolment =>
                enrolment.getIdentifier(eoriIdentifier).map(identifier => EORI(identifier.value))
              ).getOrElse(
                throw InsufficientEnrolments(
                  s"User does not have enrolment ${config.eoriIntegration.enrolmentKey} with EORI"
                )
              )
            )
          else
            None

        block(
          IdentifierRequest(
            request,
            Identification(
              hc.sessionId.map(_.value).getOrElse(throw new UnauthorizedException("Unable to retrieve session Id")),
              eori
            )
          )
        )

    } recover {
      case _: InsufficientEnrolments =>
        Redirect(config.eoriIntegration.enrolmentUrl.getOrElse(routes.UnauthorisedController.onPageLoad().url))
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }

  }

}
