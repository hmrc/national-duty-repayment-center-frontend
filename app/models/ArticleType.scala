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

sealed trait ArticleType

object ArticleType extends Enumerable.Implicits {

  case object OverPaymentOfDutyOrVAT extends WithName("117") with ArticleType
  case object ErrorByCustoms extends WithName("119") with ArticleType
  case object ExceptionalCircumstances extends WithName("120") with ArticleType
  case object Schedule extends WithName("990") with ArticleType

  val values: Seq[ArticleType] = Seq(
    OverPaymentOfDutyOrVAT, ErrorByCustoms, ExceptionalCircumstances
  )

  val options: Seq[RadioOption] = values.map {
    value =>
      RadioOption("articleType", value.toString)
  }

  implicit val enumerable: Enumerable[ArticleType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
