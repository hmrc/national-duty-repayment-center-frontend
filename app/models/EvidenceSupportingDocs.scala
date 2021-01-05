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

import viewmodels.RadioOption

sealed trait EvidenceSupportingDocs

object EvidenceSupportingDocs extends Enumerable.Implicits {

  case object Invoice extends WithName("01") with EvidenceSupportingDocs
  case object TransportDocuments extends WithName("02") with EvidenceSupportingDocs
  case object CopyOfC88 extends WithName("03") with EvidenceSupportingDocs
  case object PackingList extends WithName("04") with EvidenceSupportingDocs
  case object ProofOfOrigin extends WithName("05") with EvidenceSupportingDocs
  case object ProofOfAuthority extends WithName("06") with EvidenceSupportingDocs
  case object SubstituteEntry extends WithName("07") with EvidenceSupportingDocs
  case object Other extends WithName("08") with EvidenceSupportingDocs

  val values: Seq[EvidenceSupportingDocs] = Seq(
    Invoice,
    TransportDocuments,
    CopyOfC88,
    PackingList,
    ProofOfOrigin,
    ProofOfAuthority,
    SubstituteEntry,
    Other
  )

  val options: Seq[RadioOption] = values.map {
    value =>
      RadioOption("evidenceSupportingDocs", value.toString)
  }

  implicit val enumerable: Enumerable[EvidenceSupportingDocs] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
