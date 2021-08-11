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

import play.api.libs.json.Format

case class ClaimDescription(value: String)

object ClaimDescription {

  implicit val format: Format[ClaimDescription] =
    JsonFormatUtils.stringFormat(ClaimDescription.apply)(_.value)

  def apply(value: String, reasons: Set[ClaimReasonType]): ClaimDescription = {
    val reasonString = s"Claim reason${if (reasons.size > 1) "s" else ""}: ${reasons.map(
      reason => s"${ClaimReasonType.abbreviation(reason)}($reason)"
    ).mkString(", ")}"
    new ClaimDescription(s"$reasonString\n\n$value")
  }

}
