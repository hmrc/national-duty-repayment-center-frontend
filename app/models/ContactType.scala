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

import play.api.libs.json._
import viewmodels.RadioOption

sealed trait ContactType

object ContactType extends Enumerable.Implicits {

  case object Email extends WithName("01") with ContactType
  case object Phone extends WithName("02") with ContactType

  val values: Seq[ContactType] = Seq(
    Email, Phone
  )

  val options: Seq[RadioOption] = values.map {
    value =>
      RadioOption("contactType", value.toString)
  }

  implicit val enumerable: Enumerable[ContactType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
