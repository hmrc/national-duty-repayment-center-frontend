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

package models

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ArticleType

object ArticleType extends Enumerable.Implicits {

  case object OverPaymentOfDutyOrVAT extends WithName("117") with ArticleType
  case object ErrorByCustoms extends WithName("119") with ArticleType
  case object ExceptionalCircumstances extends WithName("120") with ArticleType

  val values: Seq[ArticleType] = Seq(
    OverPaymentOfDutyOrVAT, ErrorByCustoms, ExceptionalCircumstances
  )

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text(messages(s"articleType.${value.toString}")),
        checked = form("value").value.contains(value.toString)
      )
  }

  implicit val enumerable: Enumerable[ArticleType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
