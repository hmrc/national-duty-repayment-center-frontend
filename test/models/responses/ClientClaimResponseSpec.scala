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

package models.responses

import org.scalatest.{FreeSpec, MustMatchers}
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError

class ClientClaimResponseSpec extends FreeSpec with MustMatchers {
  "ClientClaimResponse" - {
    "correctly reports Success" in {
      ClientClaimResponse("id", Some("caseRef"), None).isSuccess mustBe true
    }

    "correctly reports not Success" in {
      errorResponseWithMessage("message").isSuccess mustBe false
    }

    "correctly reports invalid case id error" in {
      errorResponseWithMessage(
        """{"errorDetail":{"errorMessage":"9xx : 03- Invalid Case ID","timestamp":"2021-07-20T11:07:03.038Z","correlationId":"a15a6424-1942-4be8-8d43-485f33ddb31c"}}"""
      ).isNotFound mustBe true
    }

    "correctly reports modified invalid case id error" in {
      errorResponseWithMessage(""""errorMessage":"9xx : 03 - Invalid Case ID"""").isNotFound mustBe true
    }

    "correctly reports closed case id error" in {
      errorResponseWithMessage(
        """{"errorDetail":{"errorMessage":"04 - Requested case already closed","timestamp":"2021-07-20T11:07:03.038Z","correlationId":"a15a6424-1942-4be8-8d43-485f33ddb31c"}}"""
      ).isCaseClosed mustBe true
    }

    "correctly reports modified closed case id error" in {
      errorResponseWithMessage(""""errorMessage":"9xx : 04-Requested case already closed"""").isCaseClosed mustBe true
    }
  }

  private def errorResponseWithMessage(message: String) =
    ClientClaimResponse("id", Some("caseRef"), Some(ApiError("code", Some(message))))

}
