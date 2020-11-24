/*
 * Copyright 2020 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import models.requests.IdentifierRequest
import models.responses._
import models.results.{InvalidJson, UnexpectedResponseStatus}
import models.{InternalId, PostcodeLookup}
import org.mockito.Matchers.{any, eq => equalTo}
import org.mockito.Mockito.{times, verify}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

class AddressLookupConnectorSpec
  extends FreeSpec
    with MustMatchers
    with WireMockHelper
    with ScalaFutures
    with EitherValues
    with IntegrationPatience
    with MockitoSugar {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  implicit private lazy val idRequest: IdentifierRequest[AnyContentAsEmpty.type] =
    IdentifierRequest(
      FakeRequest("", "").withHeaders(HeaderNames.USER_AGENT -> "User Agent"),
      "identifier"
    )

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.address-lookup.port" -> server.port
      )
      .build()

  def addressJson(id: String): String =
    s"""{
       |    "id": "$id",
       |    "uprn": 200000706253,
       |    "address": {
       |        "lines": [
       |            "line 1",
       |            "line 2"
       |        ],
       |        "town": "ABC Town",
       |        "county": "ABC County",
       |        "postcode": "ZZ99 1AA",
       |        "subdivision": {
       |            "code": "ZZ-Code",
       |            "name": "ZZ-Name"
       |        },
       |        "country": {
       |            "code": "ZZ",
       |            "name": "ZZ-Country"
       |        }
       |    },
       |    "localCustodian": {
       |        "code": 1760,
       |        "name": "ABC-Name"
       |    },
       |    "location": [
       |        50.9986451,
       |        -1.4690977
       |    ],
       |    "language": "en"
       |}""".stripMargin

  var happyJson: String =
    s"""[
       |${addressJson("GB200000706253")},
       |${addressJson("GB200000706254")},
       |${addressJson("GB200000706255")}
       |]""".stripMargin

  "AddressLookupConnector" - {
    "when calling the addressLookup method" - {

      "and valid json is returned" - {

        "must return a Right(SuccessResponse)" in {

          val app = application
          running(app) {

            val connector = app.injector.instanceOf[AddressLookupConnector]

            server.stubFor(
              get(urlEqualTo("/v2/uk/addresses?postcode=AA11ZZ")).willReturn(ok(happyJson))
            )

            val result = connector.addressLookup(PostcodeLookup("AA11ZZ", None)).futureValue

            val expectedAddress = LookedUpAddress(Seq("line 1", "line 2"), "ABC Town", Some("ABC County"), "ZZ99 1AA")
            result mustBe Right(AddressLookupResponseModel(Seq(
              LookedUpAddressWrapper("GB200000706253", Uprn(200000706253L), expectedAddress, "en", Some(Location(50.9986451, -1.4690977))),
              LookedUpAddressWrapper("GB200000706254", Uprn(200000706253L), expectedAddress, "en", Some(Location(50.9986451, -1.4690977))),
              LookedUpAddressWrapper("GB200000706255", Uprn(200000706253L), expectedAddress, "en", Some(Location(50.9986451, -1.4690977))),
            )))

          }
        }
      }

      "when invalid json is returned" - {

        "must return a Left(InvalidJson)" in {

          val app = application
          running(app) {

            val connector = app.injector.instanceOf[AddressLookupConnector]

            val invalidJson = """{"foo" : "bar"}"""
            server.stubFor(
              get(urlEqualTo("/v2/uk/addresses?postcode=AA11ZZ")).willReturn(ok(invalidJson))
            )

            val result = connector.addressLookup(PostcodeLookup("AA11ZZ", None)).futureValue

            result mustBe Left(InvalidJson)
          }
        }
      }

      "when an error is returned" - {

        "must return a Left(UnexpectedFailure)" in {

          val app = application
          running(app) {

            val connector = app.injector.instanceOf[AddressLookupConnector]

            server.stubFor(
              get(urlEqualTo("/v2/uk/addresses?postcode=AA11ZZ")).willReturn(serverError())
            )

            val result = connector.addressLookup(PostcodeLookup("AA11ZZ", None)).futureValue

            result mustBe Left(UnexpectedResponseStatus(500, "Unexpected response, status 500 returned"))
          }
        }
      }
    }

  }
}
