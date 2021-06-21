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

package service

import java.time.LocalDateTime

import base.SpecBase
import connectors.NDRCConnector
import data.TestData.{populateUserAnswersRepresentativeWithEmail, populateUserAnswersWithAmendData}
import models.requests.{AmendClaimBuilder, CreateClaimBuilder, DataRequest}
import models.responses.{ClientClaimSuccessResponse, NDRCFileTransferResult}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.ClaimService
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError

import scala.concurrent.{ExecutionContext, Future}

class ClaimServiceSpec extends SpecBase with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  val createClaimBuilder = injector.instanceOf[CreateClaimBuilder]
  val amendClaimBuilder  = injector.instanceOf[AmendClaimBuilder]

  "Claim service" should {

    "should return caseID when create case is successful" in {
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      implicit val hc: HeaderCarrier   = HeaderCarrier()
      implicit val request: Request[_] = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")
      val dataRequest                  = DataRequest(request, "12", testUserAnswers)

      val connector = mock[NDRCConnector]
      val response  = ClientClaimSuccessResponse("1", None, Some(NDRCFileTransferResult("caseId", LocalDateTime.now)))
      when(connector.submitClaim(any(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitClaim(testUserAnswers)(hc, dataRequest).futureValue
      result mustBe "caseId"
    }

    "should throw error when duplicate caseID is submitted" in {
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      implicit val hc: HeaderCarrier   = HeaderCarrier()
      implicit val request: Request[_] = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")
      val dataRequest                  = DataRequest(request, "12", testUserAnswers)

      val connector = mock[NDRCConnector]
      val response  = ClientClaimSuccessResponse("1", Some(ApiError("409", Some("Aa"))), None)
      when(connector.submitClaim(any(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val thrown = intercept[RuntimeException] {
        service.submitClaim(testUserAnswers)(hc, dataRequest).futureValue
      }
      thrown.getMessage contains "Case already exists Aa"
    }

    "should throw exception when unknown error returned" in {
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      implicit val hc: HeaderCarrier   = HeaderCarrier()
      implicit val request: Request[_] = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")
      val dataRequest                  = DataRequest(request, "12", testUserAnswers)

      val connector = mock[NDRCConnector]
      val response  = ClientClaimSuccessResponse("1", Some(ApiError("500", Some("Aa"))), None)
      when(connector.submitClaim(any(), any())(any())).thenReturn(Future.successful(response))
      val message = response.error.map(_.errorCode).map(_ + " ").getOrElse("") +
        response.error.map(_.errorMessage).getOrElse("")

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val thrown = intercept[RuntimeException] {
        service.submitClaim(testUserAnswers)(hc, dataRequest).futureValue
      }
      thrown.getMessage contains message
    }

    "should return caseID when amend case is successful" in {
      val testUserAnswers = populateUserAnswersWithAmendData(emptyUserAnswers)

      implicit val hc: HeaderCarrier   = HeaderCarrier()
      implicit val request: Request[_] = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")
      val dataRequest                  = DataRequest(request, "12", testUserAnswers)

      val connector = mock[NDRCConnector]
      val response  = ClientClaimSuccessResponse("1", None, Some(NDRCFileTransferResult("caseId", LocalDateTime.now)))
      when(connector.submitAmendClaim(any(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitAmendClaim(testUserAnswers)(hc, dataRequest).futureValue
      result mustBe "caseId"
    }

    "should throw exception when unknown error returned for amend case" in {
      val testUserAnswers = populateUserAnswersWithAmendData(emptyUserAnswers)

      implicit val hc: HeaderCarrier   = HeaderCarrier()
      implicit val request: Request[_] = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")
      val dataRequest                  = DataRequest(request, "12", testUserAnswers)

      val connector = mock[NDRCConnector]
      val response  = ClientClaimSuccessResponse("1", Some(ApiError("500", Some("Aa"))), None)
      when(connector.submitAmendClaim(any(), any())(any())).thenReturn(Future.successful(response))
      val message = response.error.map(_.errorCode).map(_ + " ").getOrElse("") +
        response.error.map(_.errorMessage).getOrElse("")

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val thrown = intercept[RuntimeException] {
        service.submitAmendClaim(testUserAnswers)(hc, dataRequest).futureValue
      }
      thrown.getMessage contains message
    }
  }
}
