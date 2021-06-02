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

import com.google.inject.Inject
import config.Service
import models.requests.{AmendClaimRequest, CreateClaimRequest}
import models.responses.ClientClaimSuccessResponse
import play.api.Configuration
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class NDRCConnector @Inject() (config: Configuration, httpClient: HttpClient)(implicit ec: ExecutionContext)
    extends HttpErrorFunctions {

  private val baseUrl = config.get[Service]("microservice.services.national-duty-repayment-center")

  def submitClaim(request: CreateClaimRequest)(implicit hc: HeaderCarrier): Future[ClientClaimSuccessResponse] =
    httpClient.POST[CreateClaimRequest, ClientClaimSuccessResponse](s"$baseUrl/create-case", request)

  def submitAmendClaim(request: AmendClaimRequest)(implicit hc: HeaderCarrier): Future[ClientClaimSuccessResponse] =
    httpClient.POST[AmendClaimRequest, ClientClaimSuccessResponse](s"$baseUrl/amend-case", request)

}
