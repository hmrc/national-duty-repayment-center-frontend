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

import com.google.inject.Inject
import config.Service
import models.requests.CreateClaimRequest
import models.responses.ClientClaimSuccessResponse
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class CreateClaimRequest {

  import play.api.libs.json.{Json, OFormat}

  class RepaymentConnector @Inject()(
                                      config: Configuration,
                                      httpClient: HttpClient
                                    )(
                                      implicit ec: ExecutionContext
                                    ) extends HttpErrorFunctions {

    private val baseUrl = config.get[Service]("microservice.services.national-duty-repayment-center-frontend")


    def submitRepayment(request: CreateClaimRequest)(implicit hc: HeaderCarrier): Future[ClientClaimSuccessResponse] = {
      val url = s"$baseUrl/discounted-dining-participant/submit-registration"

      httpClient.POST[CreateClaimRequest, ClientClaimSuccessResponse](url, request)
    }

    def getRepayment(request: CreateClaimRequest)(implicit hc: HeaderCarrier): Future[Option[ClientClaimSuccessResponse]] = {
      val url = s"$baseUrl/discounted-dining-participant/get-registration"

      httpClient.POST[CreateClaimRequest, ClientClaimSuccessResponse](url, request).map(Some(_))
    }.recover {
      case _: NotFoundException => None
    }
  }

}