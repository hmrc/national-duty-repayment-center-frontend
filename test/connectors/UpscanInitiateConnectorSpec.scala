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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.BankDetails
import models.bars.{AssessBusinessBankDetailsRequest, AssessBusinessBankDetailsResponse}
import org.scalatest.MustMatchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class UpscanInitiateConnectorSpec extends SpecBase with WireMockHelper with MustMatchers {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.upscan-initiate.port" -> server.port)
      .build()

  "initiate" must {

    "must return a result when the server responds with OK" in {
      val app = application
      running(app) {

        val url = "/upscan/v2/initiate"
        val responseBody =
          s"""{
             |    "reference": "11370e18-6e24-453e-b45a-76d3e32ea33d",
             |    "uploadRequest": {
             |        "href": "https://xxxx/upscan-upload-proxy/bucketName",
             |        "fields": {
             |            "Content-Type": "application/xml",
             |            "success_action_redirect": "https://myservice.com/nextPage",
             |            "error_action_redirect": "https://myservice.com/errorPage"
             |        }
             |    }
             |}""".stripMargin
        val connector = app.injector.instanceOf[UpscanInitiateConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.initiate(UpscanInitiateRequest("/some-callback-url")).futureValue

        result.reference mustBe "11370e18-6e24-453e-b45a-76d3e32ea33d"
      }
    }
  }

}
