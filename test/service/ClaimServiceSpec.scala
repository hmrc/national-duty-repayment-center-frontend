/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase
import connectors.NDRCConnector
import data.TestData.{populateUserAnswersRepresentativeWithEmail, populateUserAnswersWithAmendData}
import models.requests.{AmendClaimBuilder, CreateClaimBuilder, CreateClaimRequest}
import models.responses.ClientClaimResponse
import models.{ClaimDescription, ClaimReasonType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ClaimReasonTypeMultiplePage, ReasonForOverpaymentPage}
import services.ClaimService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

class ClaimServiceSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks with OptionValues {

  val createClaimBuilder = injector.instanceOf[CreateClaimBuilder]
  val amendClaimBuilder  = injector.instanceOf[AmendClaimBuilder]
  val uuidMatcher: Regex = "[A-Za-z0-9-]{36}".r

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

    "should throw Exception when create case is unsuccessful" in {
      val testUserAnswers = emptyUserAnswers

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]

      val service   = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val exception = intercept[RuntimeException](service.submitClaim(testUserAnswers)(hc))
      exception.getMessage mustBe "UserAnswers did not contain sufficient data to construct CreateClaimRequest"
    }

    "should create a UUID based correlationId for create claim" in {

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)

      val connector                             = mock[NDRCConnector]
      val response                              = ClientClaimResponse("1", Some("ABC321"))
      val correlationId: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(connector.submitClaim(any(), correlationId.capture())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitClaim(testUserAnswers)(hc).futureValue
      result mustBe "ABC321"

      correlationId.getValue.length mustBe 36
      uuidMatcher.findFirstMatchIn(correlationId.getValue) must not be empty

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

    "should create a UUID based correlationId for amend claim" in {

      val testUserAnswers            = populateUserAnswersWithAmendData(emptyUserAnswers)
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector                             = mock[NDRCConnector]
      val response                              = ClientClaimResponse("1", Some("caseId"))
      val correlationId: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      when(connector.submitAmendClaim(any(), correlationId.capture())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitAmendClaim(testUserAnswers)(hc).futureValue
      result mustBe response

      correlationId.getValue.length mustBe 36
      uuidMatcher.findFirstMatchIn(correlationId.getValue) must not be empty

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

    "should throw exception when error returned for amend case" in {
      val testUserAnswers = emptyUserAnswers

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector = mock[NDRCConnector]
      val service   = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val thrown = intercept[RuntimeException] {
        service.submitAmendClaim(testUserAnswers)(hc).futureValue
      }
      thrown.getMessage contains "UserAnswers did not contain sufficient data to construct AmendClaimRequest"
    }

    "should submit an updated ClaimDescription" in {

      val reasons: Set[ClaimReasonType] = Set(ClaimReasonType.Preference, ClaimReasonType.Value)
      val testUserAnswers = populateUserAnswersRepresentativeWithEmail(emptyUserAnswers)
        .set(ReasonForOverpaymentPage, ClaimDescription("some description")).success.value
        .set(ClaimReasonTypeMultiplePage, reasons).success.value

      implicit val hc: HeaderCarrier = HeaderCarrier()

      val connector                                   = mock[NDRCConnector]
      val response                                    = ClientClaimResponse("1", Some("ABC123"))
      val request: ArgumentCaptor[CreateClaimRequest] = ArgumentCaptor.forClass(classOf[CreateClaimRequest])
      when(connector.submitClaim(request.capture(), any())(any())).thenReturn(Future.successful(response))

      val service = new ClaimService(connector, createClaimBuilder, amendClaimBuilder)(ExecutionContext.global)
      val result  = service.submitClaim(testUserAnswers)(hc).futureValue
      result mustBe "ABC123"

      request.getValue.Content.ClaimDetails.ClaimDescription mustBe ClaimDescription("some description", reasons)

    }
  }
}
