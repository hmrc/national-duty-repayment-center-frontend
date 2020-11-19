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

import config.FrontendAppConfig
import connectors.httpparsers.AddressLookupHttpParser
import connectors.httpparsers.AddressLookupHttpParser.AddressLookupResponse
import javax.inject.Inject
import models.PostcodeLookup
import models.audit.AddressLookupAuditModel
import models.requests.RequestWithInternalId
import play.api.http.HeaderNames
import play.api.libs.json.{JsValue, Json}
import services.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AddressLookupConnector @Inject()(httpClient: HttpClient,
                                       auditService: AuditService)
                                      (implicit appConfig: FrontendAppConfig,
                                       ec: ExecutionContext) {
  private def getResponseJson(response: HttpResponse): JsValue = Try(response.json).getOrElse(Json.obj())

  def addressLookup(query: PostcodeLookup)
                   (implicit hc: HeaderCarrier, request: RequestWithInternalId[_]): Future[AddressLookupResponse] = {
    lazy val url = appConfig.addressLookupServiceUrl.baseUrl + "/v2/uk/addresses"

    val urlParams = Seq(
      Some("postcode" -> query.postCodeTrimmed),
      query.houseNumber.map("filter" -> _)
    ).collect { case Some(x) => x }


    httpClient.GET(
      url = url,
      queryParams = urlParams,
      headers = Seq(HeaderNames.USER_AGENT -> appConfig.appName)
    )(AddressLookupHttpParser.AddressLookupReads, hc, ec).map {
      case (connectorResponse, httpResponse) =>
        auditService.audit(AddressLookupAuditModel(
          request.internalId.value,
          getResponseJson(httpResponse),
          request.headers.get(HeaderNames.USER_AGENT).getOrElse("UNKNOWN")
        ))
        connectorResponse
    }
  }
}
