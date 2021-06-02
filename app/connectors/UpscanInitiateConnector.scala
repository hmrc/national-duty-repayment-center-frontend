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

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import config.FrontendAppConfig
import play.api.Logger
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

/**
  * Connects to the upscan-initiate service API.
  */
import javax.inject.{Inject, Singleton}

@Singleton
class UpscanInitiateConnector @Inject() (appConfig: FrontendAppConfig, http: HttpGet with HttpPost, metrics: Metrics)
    extends HttpAPIMonitor {

  val baseUrl: String                          = appConfig.upscanInitiateBaseUrl
  val upscanInitiatev2Path                     = "/upscan/v2/initiate"
  val userAgent                                = "national-duty-repayment-center-frontend"
  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def initiate(
    request: UpscanInitiateRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UpscanInitiateResponse] =
    monitor(s"ConsumedAPI-upscan-v2-initiate-POST") {
      http
        .POST[UpscanInitiateRequest, UpscanInitiateResponse](
          new URL(baseUrl + upscanInitiatev2Path).toExternalForm,
          request
        )
        .recoverWith {
          case e: Throwable =>
            Logger.error(s"Upscan initiate request failed due to: $e")
            Future.failed(UpscanInitiateError(e))
        }
    }

}

case class UpscanInitiateError(e: Throwable) extends RuntimeException(e)
