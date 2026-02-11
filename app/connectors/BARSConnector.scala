/*
 * Copyright 2026 HM Revenue & Customs
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
import models.bars.{AssessBusinessBankDetailsRequest, AssessBusinessBankDetailsResponse}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BARSConnector @Inject() (http: HttpClientV2, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def assessBusinessBankDetails(
    request: AssessBusinessBankDetailsRequest
  )(implicit hc: HeaderCarrier): Future[AssessBusinessBankDetailsResponse] =
    http
      .post(url"${appConfig.barsBusinessAssessUrl}")
      .withBody(Json.toJson(request))
      .execute[AssessBusinessBankDetailsResponse]

}
