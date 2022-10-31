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

import base.SpecBase
import config.FrontendAppConfigImpl
import controllers.routes
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, spy, when}
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import play.api.i18n.Langs
import play.api.mvc.{BodyParsers, Result, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec extends SpecBase with BeforeAndAfterEach {

  val authConnector: AuthConnector = mock[AuthConnector]

  val realConfig                = injector.instanceOf[Configuration]
  val realLangs                 = injector.instanceOf[Langs]
  val mockConfig: Configuration = spy(realConfig)
  def appConfig                 = new FrontendAppConfigImpl(mockConfig, realLangs)

  val enrolmentsWithoutEORI: Enrolments = Enrolments(
    Set(Enrolment(key = "IR-SA", identifiers = Seq(EnrolmentIdentifier("UTR", "123")), state = "Activated"))
  )

  val usersEORI: String = "GB1234567890"

  val enrolmentsWithEORI: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "HMRC-CTS-ORG",
        identifiers = Seq(EnrolmentIdentifier("EORINumber", usersEORI)),
        state = "Activated"
      )
    )
  )

  val bodyParsers = injector.instanceOf[BodyParsers.Default]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad() = authAction(_ => Results.Ok)
  }

  override protected def beforeEach(): Unit =
    super.beforeEach()

  override protected def afterEach(): Unit = {
    reset(authConnector, mockConfig)
    super.afterEach()
  }

  "Auth Action" when {

    "the user hasn't logged in" must {

      "redirect the user to log in " in {

        val result: Future[Result] = handleAuthError(MissingBearerToken())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }

    }

    "the user's session has expired" must {

      "redirect the user to log in " in {

        val result: Future[Result] = handleAuthError(BearerTokenExpired())

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient confidence level" must {

      "redirect the user to the unauthorised page" in {

        val result: Future[Result] = handleAuthError(InsufficientConfidenceLevel())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user used an unaccepted auth provider" must {

      "redirect the user to the unauthorised page" in {

        val result: Future[Result] = handleAuthError(UnsupportedAuthProvider())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported affinity group" must {

      "redirect the user to the unauthorised page" in {

        val result: Future[Result] = handleAuthError(UnsupportedAffinityGroup())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user has an unsupported credential role" must {

      "redirect the user to the unauthorised page" in {

        val result: Future[Result] = handleAuthError(UnsupportedCredentialRole())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user doesn't have sufficient enrolments and EoriIntegration not enabled" must {

      "redirect the user to the unauthorised page" in {

        whenEoriIntegrationEnabled(false)

        val result: Future[Result] = handleAuthError(InsufficientEnrolments())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "the user doesn't have sufficient enrolments and EoriIntegration enabled" must {

      "redirect the user to the enrolment page" in {

        whenEoriIntegrationEnabled(true)

        val result: Future[Result] = handleAuthError(InsufficientEnrolments())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some("/some-enrolment-url")
      }
    }

    "the user has the wrong enrolment and EoriIntegration enabled" must {

      "redirect the user to the enrolment page" in {

        whenEoriIntegrationEnabled(true)

        val result: Future[Result] = handleAuthWithEnrolments(enrolmentsWithoutEORI)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some("/some-enrolment-url")
      }
    }

    "the user has correct enrolment and EoriIntegration enabled" must {

      "redirect the user to the next page" in {

        whenEoriIntegrationEnabled(true)

        val result: Future[Result] = handleAuthWithEnrolments(enrolmentsWithEORI)

        status(result) mustBe OK
      }
    }
  }

  private def handleAuthError(exc: AuthorisationException): Future[Result] = {

    when(authConnector.authorise[Enrolments](any(), any())(any(), any()))
      .thenReturn(Future.failed(exc))

    val authAction = new AuthenticatedIdentifierAction(authConnector, appConfig, bodyParsers)
    val controller = new Harness(authAction)
    controller.onPageLoad()(fakeRequest)
  }

  private def handleAuthWithEnrolments(enrolments: Enrolments): Future[Result] = {
    when(authConnector.authorise(any(), ArgumentMatchers.eq(allEnrolments))(any(), any())).thenReturn(
      Future.successful(enrolments)
    )

    val authAction = new AuthenticatedIdentifierAction(authConnector, appConfig, bodyParsers)
    val controller = new Harness(authAction)
    val request    = fakeRequest.withSession(SessionKeys.sessionId -> "sessionId")
    controller.onPageLoad()(request)
  }

  private def whenEoriIntegrationEnabled(enabled: Boolean) = {
    when(mockConfig.get[Boolean]("eori-integration.enabled")).thenReturn(enabled)
    when(mockConfig.get[String]("eori-integration.enrolment-url")).thenReturn("/some-enrolment-url")
  }

}
