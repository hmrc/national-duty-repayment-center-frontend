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

import java.util.UUID

import base.SpecBase
import connectors.NDRCConnector
import data.TestData.{populateUserAnswersRepresentativeWithEmail, populateUserAnswersWithAmendData}
import models.requests.{AmendClaimBuilder, CreateClaimBuilder}
import models.responses.ClientClaimResponse
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import services.ClaimService
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError

import scala.concurrent.{ExecutionContext, Future}

class ClaimServiceSpec extends SpecBase with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  val createClaimBuilder = injector.instanceOf[CreateClaimBuilder]
  val amendClaimBuilder  = injector.instanceOf[AmendClaimBuilder]

  "Claim service" should {

    "should return caseID when create case is successful" in {
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]
      val response  = ClientClaimResponse("1", Some("ABC123"))
      when(connector.submitClaim(any(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitClaim(testUserAnswers)(hc).futureValue
      result mustBe "ABC123"
    }

    "should throw error when duplicate caseID is submitted" in {
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]
      val response  = ClientClaimResponse("1", None, Some(ApiError("409", Some("Aa"))))
      when(connector.submitClaim(any(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val thrown = intercept[RuntimeException] {
        service.submitClaim(testUserAnswers)(hc).futureValue
      }
      thrown.getMessage contains "Case already exists Aa"
    }

    "should throw exception when unknown error returned" in {
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]
      val response  = ClientClaimResponse("1", None, Some(ApiError("500", Some("Aa"))))
      when(connector.submitClaim(any(), any())(any())).thenReturn(Future.successful(response))
      val message = response.error.map(_.errorCode).map(_ + " ").getOrElse("") +
        response.error.map(_.errorMessage).getOrElse("")

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val thrown = intercept[RuntimeException] {
        service.submitClaim(testUserAnswers)(hc).futureValue
      }
      thrown.getMessage contains message
    }

    "should return claim response when amend case is successful" in {
      val testUserAnswers = populateUserAnswersWithAmendData(emptyUserAnswers)

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]
      val response  = ClientClaimResponse("1", Some("caseId"))
      when(connector.submitAmendClaim(any(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitAmendClaim(testUserAnswers)(hc).futureValue
      result mustBe response
    }

    "should return failed claim response when amend case is a 'known' failure" in {
      val testUserAnswers = populateUserAnswersWithAmendData(emptyUserAnswers)

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]
      val response  = ClientClaimResponse("1", None, Some(ApiError("400", Some("04 - Requested case already closed"))))
      when(connector.submitAmendClaim(any(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitAmendClaim(testUserAnswers)(hc).futureValue
      result mustBe response
    }

    "should throw exception when unknown error returned for amend case" in {
      val testUserAnswers = populateUserAnswersWithAmendData(emptyUserAnswers)

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]
      val response  = ClientClaimResponse("1", None, Some(ApiError("500", Some("Aa"))))
      when(connector.submitAmendClaim(any(), any())(any())).thenReturn(Future.successful(response))
      val message = response.error.map(_.errorCode).map(_ + " ").getOrElse("") +
        response.error.map(_.errorMessage).getOrElse("")

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val thrown = intercept[RuntimeException] {
        service.submitAmendClaim(testUserAnswers)(hc).futureValue
      }
      thrown.getMessage contains message
    }

    "should create a trimmed correlationId from the header carrier" in {

      val uuid = UUID.randomUUID().toString
      uuid.length mustBe 36
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("extra-" + uuid)))

      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      val connector                             = mock[NDRCConnector]
      val response                              = ClientClaimResponse("1", Some("ABC123"))
      val correlationId: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(connector.submitClaim(any(), correlationId.capture())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitClaim(testUserAnswers)(hc).futureValue
      result mustBe "ABC123"

      correlationId.getValue mustBe uuid

    }

    "should not fail if the request id is less than 36 characters" in {

      val requestId                  = "1234"
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId(requestId)))

      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      val connector                             = mock[NDRCConnector]
      val response                              = ClientClaimResponse("1", Some("ABC123"))
      val correlationId: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(connector.submitClaim(any(), correlationId.capture())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitClaim(testUserAnswers)(hc).futureValue
      result mustBe "ABC123"

      correlationId.getValue mustBe requestId

    }
  }
}
