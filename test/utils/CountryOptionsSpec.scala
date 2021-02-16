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

package utils

import base.SpecBase
import com.kenshoo.play.metrics.{Metrics, MetricsImpl}
import com.typesafe.config.ConfigException
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRequiredActionImpl, DataRetrievalAction, FakeDataRetrievalAction, FakeIdentifierAction, IdentifierAction}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem

class CountryOptionsSpec extends SpecBase {

  "Country Options" must {

    "build correctly the SelectItems with country list and country code" in {
      val app =
        new GuiceApplicationBuilder()
          .configure(
            "location.canonical.list" -> "country-canonical-list-test.json",
            "metrics.enabled" -> false,
          )
          .overrides(
            bind[Metrics].to[MetricsImpl]
          ).build()

      running(app) {

        val countryOption: CountryOptions = app.injector.instanceOf[CountryOptions]
        countryOption.options mustEqual Seq(SelectItem(Some("GB"), "United Kingdom"),
          SelectItem(Some("AF"), "Afghanistan"))
      }
      app.stop()
    }

    "throw the error if the country json does not exist" in {
      val builder = new GuiceApplicationBuilder()
        .configure(
          "location.canonical.list" -> "country-canonical-test.json",
          "metrics.enabled" -> false,
        )

      an[ConfigException.BadValue] shouldBe thrownBy {
        new CountryOptions(builder.environment, builder.injector.instanceOf[FrontendAppConfig])
      }
    }
  }
}
