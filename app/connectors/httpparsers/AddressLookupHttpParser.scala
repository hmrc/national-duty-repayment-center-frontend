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

package connectors.httpparsers

import models.responses.AddressLookupResponseModel
import models.results.{ConnectorErrorResult, InvalidJson, UnexpectedResponseStatus}
import org.slf4j.LoggerFactory
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object AddressLookupHttpParser {
  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  type AddressLookupResponse = Either[ConnectorErrorResult, AddressLookupResponseModel]

  implicit object AddressLookupReads extends HttpReads[(AddressLookupResponse, HttpResponse)] {

    def read(method: String, url: String, response: HttpResponse): (AddressLookupResponse, HttpResponse) =
      (response.status match {
        case OK =>
          response.json.validate[AddressLookupResponseModel] match {
            case JsSuccess(model, _) =>
              Right(model)
            case JsError(errors) =>
              logger.warn("Failed trying to parse JSON", errors)
              Left(InvalidJson)
          }
        case status =>
          logger.warn(s"Unexpected response, status $status returned")
          Left(UnexpectedResponseStatus(status, s"Unexpected response, status $status returned"))
      }) -> response

  }

}
