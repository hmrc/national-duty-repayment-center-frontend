/*
 * Copyright 2022 HM Revenue & Customs
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

import models.NumberOfEntriesType.Multiple
import play.api.libs.json.{Json, OFormat}

import scala.util.Try

case class Entries(numberOfEntriesType: NumberOfEntriesType, entries: Option[String]) {

  val isMultipleSmall: Boolean =
    numberOfEntriesType == Multiple && entries.flatMap(ent => Try(ent.toInt).toOption).exists(num => num <= 10)

  val isMultipleLarge: Boolean = numberOfEntriesType == Multiple && !isMultipleSmall
}

object Entries {
  implicit val format: OFormat[Entries] = Json.format[Entries]
}
