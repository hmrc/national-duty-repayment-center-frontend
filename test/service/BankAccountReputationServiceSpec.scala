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
import connectors.BARSConnector
import data.BarsTestData
import data.TestData._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import services.BankAccountReputationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BankAccountReputationServiceSpec extends SpecBase with Matchers with BeforeAndAfterEach with BarsTestData {

  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  private val connector = mock[BARSConnector]

  private def service = new BankAccountReputationService(connector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(connector.assessBusinessBankDetails(any())(any())).thenReturn(Future.successful(validAssessResponse))
  }

  override protected def afterEach(): Unit = {
    reset(connector)
    super.afterEach()
  }

  "BankAccountReputationService" should {

    "return valid response when BARs returns valid response" in {
      service.validate(testBankDetails).futureValue.isValid mustBe true

      verify(connector).assessBusinessBankDetails(any())(any())
    }

    "return invalid response when BARs returns invalid response" in {
      when(connector.assessBusinessBankDetails(any())(any())).thenReturn(
        Future.successful(invalidAccountNumberAssessResponse)
      )

      service.validate(testBankDetails).futureValue.isValid mustBe false

      verify(connector).assessBusinessBankDetails(any())(any())
    }

  }
}
