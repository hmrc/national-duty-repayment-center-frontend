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

package models

import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.RadioOption

sealed trait ClaimantType

object ClaimantType extends Enumerable.Implicits {

  case object Importer extends WithName("01") with ClaimantType
  case object Representative extends WithName("02") with ClaimantType

  val values: Seq[ClaimantType] = Seq(
    Importer, Representative
  )

//  val options: Seq[RadioOption] = values.map {
//    value =>
//      RadioOption("claimantType", value.toString)
//  }

  val options: Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text("Test"), //TODO messages("claimantType" + value)
        checked = false //TODO
      )
  }

  implicit val enumerable: Enumerable[ClaimantType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
