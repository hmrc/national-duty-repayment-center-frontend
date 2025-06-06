/*
 * Copyright 2025 HM Revenue & Customs
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

import viewmodels.RadioOption

sealed trait BulkFileUpload

object BulkFileUpload extends Enumerable.Implicits {

  case object EPU         extends WithName("01") with BulkFileUpload
  case object ItemNumbers extends WithName("02") with BulkFileUpload
  case object TypeOfDuty  extends WithName("03") with BulkFileUpload
  case object AmountPaid  extends WithName("04") with BulkFileUpload
  case object AmountDue   extends WithName("05") with BulkFileUpload

  val values: Seq[BulkFileUpload] = Seq(EPU, ItemNumbers, TypeOfDuty, AmountPaid, AmountDue)

  val options: Seq[RadioOption] = values.map {
    value =>
      RadioOption("bulkFileUpload", value.toString)
  }

  implicit val enumerable: Enumerable[BulkFileUpload] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
