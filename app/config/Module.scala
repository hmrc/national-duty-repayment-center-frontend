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

package config

import akka.actor.ActorSystem
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Inject, Singleton}
import com.typesafe.config.Config
import controllers.CheckStateActor
import controllers.actions._
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.libs.ws.WSClient
import repositories.{DefaultSessionRepository, SessionRepository}
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HttpGet, HttpPost}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.util.matching.Regex

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[HttpGet]).to(classOf[CustomHttpClient])
    bind(classOf[HttpPost]).to(classOf[CustomHttpClient])
    bind(classOf[FrontendAppConfig]).to(classOf[FrontendAppConfigImpl]).asEagerSingleton()
    bindActor[CheckStateActor]("check-state-actor")

    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()

    // For session based storage instead of cred based, change to SessionIdentifierAction
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()

    bind(classOf[SessionRepository]).to(classOf[DefaultSessionRepository]).asEagerSingleton()

  }
}
@Singleton
class CustomHttpAuditing @Inject() (
                                     val auditConnector: AuditConnector,
                                     @Named("appName") val appName: String
                                   ) extends HttpAuditing {

  override val auditDisabledForPattern: Regex =
    """.*?\/auth\/authorise$""".r

}
@Singleton
class CustomHttpClient @Inject() (
                                   config: Configuration,
                                   val httpAuditing: CustomHttpAuditing,
                                   override val wsClient: WSClient,
                                   override protected val actorSystem: ActorSystem
                                 ) extends uk.gov.hmrc.http.HttpClient with WSHttp {

  override lazy val configuration: Option[Config] = Option(config.underlying)

  override val hooks: Seq[HttpHook] = Seq(httpAuditing.AuditingHook)
}