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

package services

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import models.Country
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem

// Adapted from Address Lookup Frontend

@ImplementedBy(classOf[ForeignOfficeCountryService])
trait CountryService {

  def findAll(welshFlag: Boolean = false): Seq[Country]

  def selectItems(welshFlag: Boolean = false) =
    findAll(welshFlag).map(country => SelectItem(text = country.name, value = Some(country.code)))

  def find(code: String, welshFlag: Boolean = false): Country

}

@Singleton
class ForeignOfficeCountryService extends CountryService {

  implicit val fcoCountryFormat = Json.format[FcoCountry]

  private val countriesEN: Seq[Country] =
    Json.parse(getClass.getResourceAsStream("/resources/countriesEN.json")).as[Map[String, FcoCountry]].map { country =>
      Country(country._2.country, country._2.name)
    }.toSeq.sortWith(_.name < _.name)

  private val countriesCY: Seq[Country] =
    Json.parse(getClass.getResourceAsStream("/resources/countriesCY.json")).as[Map[String, FcoCountry]].map { country =>
      Country(country._2.country, country._2.name)
    }.toSeq.sortWith(_.name < _.name)

  override def findAll(welshFlag: Boolean = false): Seq[Country] =
    if (!welshFlag) countriesEN
    else countriesCY

  override def find(code: String, welshFlag: Boolean = false): Country =
    if (!welshFlag) {
      val filtered = countriesEN.filter(_.code == code)
      filtered.head
    } else {
      val filtered = countriesCY.filter(_.code == code)
      filtered.head
    }

}

case class FcoCountry(country: String, name: String)

object ForeignOfficeCountryService extends ForeignOfficeCountryService
