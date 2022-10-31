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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.BankDetails
import models.bars.{AssessBusinessBankDetailsRequest, AssessBusinessBankDetailsResponse}
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class BARSConnectorSpec extends SpecBase with WireMockHelper with Matchers {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.bank-account-reputation.port" -> server.port)
      .build()

  "assessBusinessBankDetails" must {

    "must return a result when the server responds with Accepted" in {
      val app = application
      running(app) {

        val url = "/verify/business"
        val responseBody =
          s"""{
             |    "sortCodeIsPresentOnEISCD" : "yes",
             |    "accountNumberIsWellFormatted" : "yes",
             |    "nonStandardAccountDetailsRequiredForBacs" : "no",
             |    "accountExists" : "yes",
             |    "nameMatches" : "yes",
             |    "sortCodeSupportsDirectCredit" : "yes"
             |}""".stripMargin
        val connector = app.injector.instanceOf[BARSConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.assessBusinessBankDetails(
          AssessBusinessBankDetailsRequest(BankDetails("name", "123456", "12345678"))
        ).futureValue

        result mustBe AssessBusinessBankDetailsResponse("yes", "yes", "no", "yes", "yes", "yes")
      }
    }

    "must return a result when the server responds with partial response for account name" in {
      val app = application
      running(app) {

        val url = "/verify/business"
        val responseBody =
          s"""{
             |    "sortCodeIsPresentOnEISCD" : "yes",
             |    "accountNumberIsWellFormatted" : "yes",
             |    "nonStandardAccountDetailsRequiredForBacs" : "no",
             |    "accountExists" : "yes",
             |    "nameMatches" : "partial",
             |    "sortCodeSupportsDirectCredit" : "yes",
             |    "accountName" : "Correct account name"
             |}""".stripMargin
        val connector = app.injector.instanceOf[BARSConnector]
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok(responseBody))
        )

        val result = connector.assessBusinessBankDetails(
          AssessBusinessBankDetailsRequest(BankDetails("name", "123456", "12345678"))
        ).futureValue

        result mustBe AssessBusinessBankDetailsResponse("yes", "yes", "no", "yes", "partial", "yes")
      }
    }
  }

}
