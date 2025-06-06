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

package connectors

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.responses.ClientClaimResponse
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError
import utils.WireMockHelper

class NDRCConnectorSpec extends SpecBase with WireMockHelper with Matchers {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.national-duty-repayment-center.port" -> server.port)
      .build()

  "SubmitClaim" must {

    "must return a result when the server responds with OK" in {
      val app = application
      running(app) {

        val url = s"/create-case"
        val responseBody =
          s"""{
             |  "correlationId": "111",
             |  "caseId": "ABC123"
             |}""".stripMargin

        val connector = app.injector.instanceOf[NDRCConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.submitClaim(createClaimRequest, "111").futureValue

        result mustEqual ClientClaimResponse(correlationId = "111", caseId = Some("ABC123"))
      }
    }

    "must return None if the backend can't find a registration with case ID" in {

      val app = application
      val url = s"/create-case"

      running(app) {
        val connector = app.injector.instanceOf[NDRCConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.submitClaim(createClaimRequest, "111").value
        result mustBe None
      }
    }
  }

  "SubmitAmendClaim" must {

    "must return a result when the server responds with OK" in {
      val app = application

      running(app) {

        val url = s"/amend-case"
        val responseBody =
          s"""{
             |  "correlationId": "111",
             |  "caseId": "DEF456"
             |}""".stripMargin
        val connector = app.injector.instanceOf[NDRCConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.submitAmendClaim(amendClaimRequest, "111").futureValue

        result mustEqual ClientClaimResponse(correlationId = "111", caseId = Some("DEF456"))
      }
    }

    "must return a failed result when the server responds with 400" in {
      val app = application

      running(app) {

        val url = s"/amend-case"
        val responseBody =
          s"""{"correlationId":"111",
             |"error":{
             |"errorCode":"ERROR_UPSTREAM_UNDEFINED",
             |"errorMessage":"9xx : 03- Invalid Case ID"
             |}}""".stripMargin
        val connector = app.injector.instanceOf[NDRCConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse.withStatus(400).withBody(responseBody))
        )

        val result = connector.submitAmendClaim(amendClaimRequest, "111").futureValue

        result mustEqual ClientClaimResponse(
          correlationId = "111",
          caseId = Some("Risk-2507"),
          error = Some(
            ApiError(
              "400",
              Some(s"POST of 'http://localhost:11111/amend-case' returned 400. Response body: '$responseBody'")
            )
          )
        )
      }
    }

    "must return None if the backend can't find a registration with case ID" in {

      val app = application
      val url = s"/amend-case"

      running(app) {
        val connector = app.injector.instanceOf[NDRCConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.submitAmendClaim(amendClaimRequest, "111").value
        result mustBe None
      }
    }
  }

}
