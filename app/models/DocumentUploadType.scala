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

sealed trait DocumentUploadType

object DocumentUploadType extends Enumerable.Implicits {
  case object Invoice extends WithName("01") with DocumentUploadType
  case object TransportDocuments extends WithName("02") with DocumentUploadType
  case object CopyOfC88 extends WithName("03") with DocumentUploadType
  case object PackingList extends WithName("04") with DocumentUploadType
  case object ProofOfOrigin extends WithName("05") with DocumentUploadType
  case object SubstituteEntry extends WithName("06") with DocumentUploadType
  case object Other extends WithName("07") with DocumentUploadType

  val values: Seq[DocumentUploadType] = Seq(
    Invoice,
    TransportDocuments,
    CopyOfC88,
    PackingList,
    ProofOfOrigin,
    SubstituteEntry,
    Other
  )

  implicit val enumerable: Enumerable[DocumentUploadType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
