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


sealed trait ClaimedUnderArticle

object ClaimedUnderArticle extends Enumerable.Implicits {
  case object OverchargedAmountsOfImportOrExportDuty extends WithName("117") with ClaimedUnderArticle
  case object ErrorByTheCompetentAuthorities extends WithName("119") with ClaimedUnderArticle
  case object Equity extends WithName("120") with ClaimedUnderArticle

  val values: Seq[ClaimedUnderArticle] = Seq(
    OverchargedAmountsOfImportOrExportDuty,
    ErrorByTheCompetentAuthorities,
    Equity
  )

  implicit val enumerable: Enumerable[ClaimedUnderArticle] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
