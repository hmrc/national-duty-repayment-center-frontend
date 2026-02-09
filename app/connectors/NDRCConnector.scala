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

import com.google.inject.Inject
import config.Service
import models.requests.{AmendClaimRequest, CreateClaimRequest}
import models.responses.ClientClaimResponse
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReadsInstances.readFromJson
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http._
import uk.gov.hmrc.nationaldutyrepaymentcenter.models.responses.ApiError

import scala.concurrent.{ExecutionContext, Future}

class NDRCConnector @Inject() (config: Configuration, http: HttpClientV2)(implicit ec: ExecutionContext)
    extends HttpErrorFunctions {

  private val baseUrl: Service = config.get[Service]("microservice.services.national-duty-repayment-center")

  def submitClaim(request: CreateClaimRequest, correlationId: String)(implicit
    hc: HeaderCarrier
  ): Future[ClientClaimResponse] =
    http
      .post(url"$baseUrl/create-case")
      .withBody(Json.toJson(request))
      .setHeader("X-Correlation-Id" -> correlationId)
      .execute[ClientClaimResponse]

  def submitAmendClaim(request: AmendClaimRequest, correlationId: String)(implicit
    hc: HeaderCarrier
  ): Future[ClientClaimResponse] =
    http
      .post(url"$baseUrl/amend-case")
      .withBody(Json.toJson(request))
      .setHeader("X-Correlation-Id" -> correlationId)
      .execute[Either[UpstreamErrorResponse, ClientClaimResponse]] map {
      case Right(response) => response
      case Left(UpstreamErrorResponse(message, 400, _, _)) =>
        ClientClaimResponse(correlationId, Some(request.Content.CaseID), Some(ApiError("400", Some(message))))
      case Left(error) => throw error
    }

}
