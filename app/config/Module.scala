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

package config

import com.google.inject.AbstractModule
import controllers.CheckStateActor
import controllers.actions._
import navigation.{CreateNavigator, CreateNavigatorImpl}
import play.api.libs.concurrent.PekkoGuiceSupport
import repositories.{CacheDataRepository, SessionRepository}

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule with PekkoGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
    bind(classOf[FrontendAppConfig]).to(classOf[FrontendAppConfigImpl]).asEagerSingleton()
    bindActor[CheckStateActor]("check-state-actor")
    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()

    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()

    bind(classOf[SessionRepository]).to(classOf[CacheDataRepository]).asEagerSingleton()
    bind(classOf[CreateNavigator]).to(classOf[CreateNavigatorImpl]).asEagerSingleton()

  }

}
