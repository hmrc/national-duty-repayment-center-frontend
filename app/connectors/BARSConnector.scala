/*
 * Copyright 2024 HM Revenue & Customs
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

import config.FrontendAppConfig
import javax.inject.Inject
import models.bars.{AssessBusinessBankDetailsRequest, AssessBusinessBankDetailsResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class BARSConnector @Inject() (httpClient: HttpClient, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def assessBusinessBankDetails(
    request: AssessBusinessBankDetailsRequest
  )(implicit hc: HeaderCarrier): Future[AssessBusinessBankDetailsResponse] =
    httpClient.POST[AssessBusinessBankDetailsRequest, AssessBusinessBankDetailsResponse](
      appConfig.barsBusinessAssessUrl,
      request
    )

}
