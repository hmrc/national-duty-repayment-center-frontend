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

package connectors

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

import org.scalatest.MustMatchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper
import com.github.tomakehurst.wiremock.client.WireMock._
import base.SpecBase
import models.ClaimId
import models.responses.{ClientClaimSuccessResponse, NDRCFileTransferResult}

import java.time.LocalDateTime

class NDRCConnectorSpec extends SpecBase with WireMockHelper with MustMatchers {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.national-duty-repayment-center.port" -> server.port)
      .build()

  "SubmitClaim" must {

    "must return a result when the server responds with OK" in {
      val app         = application
      val generatedAt = LocalDateTime.now
      running(app) {

        val url = s"/create-case"
        val responseBody =
          s"""{
             |  "correlationId": "111",
             |  "result": {
             |      "caseId": "1",
             |      "generatedAt": "${generatedAt.toString}"
             |  }
             |}""".stripMargin

        val connector = app.injector.instanceOf[NDRCConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.submitClaim(createClaimRequest).futureValue

        result mustEqual ClientClaimSuccessResponse(
          correlationId = "111",
          result = Some(NDRCFileTransferResult("1", generatedAt))
        )
      }
    }

    "must return None if the backend can't find a registration with case ID" in {

      val app = application
      val url = s"/create-case"

      running(app) {
        val connector = app.injector.instanceOf[NDRCConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.submitClaim(createClaimRequest).value
        result mustBe None
      }
    }
  }

  "SubmitAmendClaim" must {

    "must return a result when the server responds with OK" in {
      val app         = application
      val generatedAt = LocalDateTime.now

      running(app) {

        val url = s"/amend-case"
        val responseBody =
          s"""{
             |  "correlationId": "111",
             |  "result": {
             |      "caseId": "1",
             |      "generatedAt": "${generatedAt.toString}"
             |  }
             |}""".stripMargin
        val connector = app.injector.instanceOf[NDRCConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.submitAmendClaim(amendClaimRequest).futureValue

        result mustEqual ClientClaimSuccessResponse(
          correlationId = "111",
          result = Some(NDRCFileTransferResult("1", generatedAt))
        )
      }
    }

    "must return None if the backend can't find a registration with case ID" in {

      val app = application
      val url = s"/amend-case"

      running(app) {
        val connector = app.injector.instanceOf[NDRCConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.submitAmendClaim(amendClaimRequest).value
        result mustBe None
      }
    }
  }

}
