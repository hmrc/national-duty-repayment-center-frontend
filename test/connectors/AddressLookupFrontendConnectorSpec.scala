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
import models.addresslookup._
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.http.HeaderNames.LOCATION
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class AddressLookupFrontendConnectorSpec extends SpecBase with WireMockHelper with Matchers {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.address-lookup-frontend.port" -> server.port)
      .build()

  "initialiseJourney" must {

    "must return a result when the server responds with Accepted" in {
      val app = application
      running(app) {

        val url       = "/api/init"
        val connector = app.injector.instanceOf[AddressLookupFrontendConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse.withStatus(202).withHeader(LOCATION, "/some-redirectUrl"))
        )

        val result = connector.initialiseJourney(AddressLookupRequestSpec.addressLookupRequest(500)).futureValue

        result mustBe AddressLookupOnRamp("/some-redirectUrl")
      }
    }

    "must error a result when the server responds with InternalServerError" in {
      val app = application
      running(app) {

        val url       = "/api/init"
        val connector = app.injector.instanceOf[AddressLookupFrontendConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse.withStatus(INTERNAL_SERVER_ERROR))
        )

        val thrown = intercept[RuntimeException] {
          connector.initialiseJourney(AddressLookupRequestSpec.addressLookupRequest(500)).futureValue
        }

        thrown.getMessage must include("Received status:500 from AddressLookupFrontend service")
      }
    }

    "must error when response does not include a location" in {
      val app = application
      running(app) {

        val url       = "/api/init"
        val connector = app.injector.instanceOf[AddressLookupFrontendConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse.withStatus(202))
        )

        val thrown = intercept[RuntimeException] {
          connector.initialiseJourney(AddressLookupRequestSpec.addressLookupRequest(500)).futureValue
        }

        thrown.getMessage must include("Missing re-direct url")
      }
    }
  }

  "getAddress" must {

    "must return a result when the server responds with Ok" in {
      val app = application
      running(app) {

        val url = "/api/confirmed?id=123"
        val responseBody =
          s"""{
             |    "auditRef" : "bed4bd24-72da-42a7-9338-f43431b7ed72",
             |    "id" : "GB990091234524",
             |    "address" : {
             |        "lines" : [ "10 Other Place", "Some District", "Anytown" ],
             |        "postcode" : "ZZ1 1ZZ",
             |        "country" : {
             |            "code" : "GB",
             |            "name" : "United Kingdom"
             |        }
             |    }
             |}""".stripMargin
        val connector = app.injector.instanceOf[AddressLookupFrontendConnector]
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.getAddress("123").futureValue

        result mustBe AddressLookupConfirmation(
          "bed4bd24-72da-42a7-9338-f43431b7ed72",
          Some("GB990091234524"),
          AddressLookupAddress(
            List("10 Other Place", "Some District", "Anytown"),
            Some("ZZ1 1ZZ"),
            AddressLookupCountry("GB", "United Kingdom")
          )
        )
      }
    }
  }

}
