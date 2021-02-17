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

import com.typesafe.config.ConfigException
import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.Address
import play.api.Environment
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem

trait CountryDropDown

@Singleton
class CountryOptions(val options: Seq[SelectItem]) extends CountryDropDown {

  @Inject()
  def this(environment: Environment, config: FrontendAppConfig) {
    this(
      environment.resourceAsStream(config.locationCanonicalList).flatMap {
        in =>
          val locationJsValue = Json.parse(in)
          Json.fromJson[Seq[Seq[String]]](locationJsValue).asOpt.map {
            _.map { countryList =>
              SelectItem(Some(countryList(1).replaceAll("country:", "")), countryList.head)
            }
          }
      }.getOrElse {
        throw new ConfigException.BadValue(config.locationCanonicalList, "country json does not exist")
      }
    )
  }

  def getCountryNameFromCode(address: Address): String = getCountryNameFromCode(address.CountryCode)

  def getCountryNameFromCode(code: String): String =
    options
      .find(_.value == code)
      .map(_.text)
      .getOrElse(code)
}
