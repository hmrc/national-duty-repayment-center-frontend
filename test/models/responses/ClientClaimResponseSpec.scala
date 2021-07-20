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
      ClientClaimResponse("id", Some("caseRef"), Some(ApiError("code", Some("message")))).isSuccess mustBe false
    }

    "correctly reports specific error" in {
      ClientClaimResponse(
        "id",
        Some("caseRef"),
        Some(
          ApiError(
            "code",
            Some(
              "9xx : 03- Invalid Case ID"
            )
          )
        )
      ).isNotFound mustBe true
    }
  }

}
