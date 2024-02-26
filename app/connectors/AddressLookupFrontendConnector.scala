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
import javax.inject.{Inject, Singleton}
import models.addresslookup.{AddressLookupConfirmation, AddressLookupOnRamp, AddressLookupRequest}
import play.api.http.HeaderNames.LOCATION
import play.api.http.Status
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import connectors.InitialiseAddressLookupHttpParser.InitialiseAddressLookupReads

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupFrontendConnector @Inject() (http: HttpClient, implicit val config: FrontendAppConfig) {

  def initialiseJourney(
    addressLookupRequest: AddressLookupRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AddressLookupOnRamp] =
    http.POST[AddressLookupRequest, AddressLookupOnRamp](config.addressLookupInitUrl, addressLookupRequest)(
      implicitly,
      InitialiseAddressLookupReads,
      hc,
      ec
    )

  private[connectors] def getAddressUrl(id: String) = s"${config.addressLookupConfirmedUrl}?id=$id"

  def getAddress(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AddressLookupConfirmation] =
    http.GET[AddressLookupConfirmation](getAddressUrl(id))(implicitly, hc, ec)

}

object InitialiseAddressLookupHttpParser {

  implicit object InitialiseAddressLookupReads extends HttpReads[AddressLookupOnRamp] {

    override def read(method: String, url: String, response: HttpResponse): AddressLookupOnRamp =
      response.status match {
        case Status.ACCEPTED =>
          response.header(LOCATION) match {
            case Some(redirectUrl) => AddressLookupOnRamp(redirectUrl)
            case None              => throw new IllegalStateException("Missing re-direct url")
          }
        case status => throw new RuntimeException(s"Received status:$status from AddressLookupFrontend service")
      }

  }

}
