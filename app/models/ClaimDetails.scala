/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

final case class ClaimDetails(
  FormType: FormType,
  CustomRegulationType: CustomsRegulationType,
  ClaimedUnderArticle: Option[ArticleType],
  ClaimedUnderRegulation: Option[UkRegulationType],
  Claimant: ClaimantType,
  ClaimType: NumberOfEntriesType,
  NoOfEntries: Option[String],
  EntryDetails: EntryDetails,
  ClaimReason: ClaimReasonType,
  ClaimDescription: ClaimDescription,
  DateReceived: LocalDate,
  ClaimDate: LocalDate,
  PayeeIndicator: WhomToPay,
  PaymentMethod: RepaymentType,
  DeclarantRefNumber: String,
  DeclarantName: String
)

object ClaimDetails {

  implicit val dateFormat: Format[LocalDate] = new Format[LocalDate] {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    override def writes(o: LocalDate): JsValue = JsString(o.format(formatter))

    override def reads(json: JsValue): JsResult[LocalDate] = json match {
      case JsString(s) =>
        Try(LocalDate.parse(s, formatter)) match {
          case Success(date)  => JsSuccess(date)
          case Failure(error) => JsError(s"Could not parse date as yyyyMMdd: ${error.getMessage}")
        }

      case other => JsError(s"Expected string but got $other")
    }

  }

  implicit val format: OFormat[ClaimDetails] = Json.format[ClaimDetails]

}
