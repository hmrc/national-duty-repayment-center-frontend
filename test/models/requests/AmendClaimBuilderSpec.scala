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

package models.requests

import base.SpecBase
import data.TestData._
import models.eis.QuoteFormatter
import org.mockito.Mockito.verify
import org.scalatest.MustMatchers
import org.scalatestplus.mockito.MockitoSugar

class AmendClaimBuilderSpec extends SpecBase with MustMatchers with MockitoSugar {

  "AmendClaimBuilder" must {
    "use QuoteFormatter when creating FurtherInformation" in {

      val formatter = mock[QuoteFormatter]

      val builder = new AmendClaimBuilder(formatter)

      val userAnswers = populateUserAnswersWithAmendData(emptyUserAnswers)

      builder.buildValidAmendRequest(userAnswers)

      verify(formatter).format(furtherInformation)

    }
  }
}
