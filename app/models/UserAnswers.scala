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

import config.FrontendAppConfig
import models.CustomsRegulationType.{UKCustomsCodeRegulation, UnionsCustomsCodeRegulation}
import models.requests.Identification
import pages._
import play.api.libs.json._
import queries.{AmendClaimIdQuery, ClaimIdQuery, Gettable, Settable}
import services.{FileUploadState, FileUploaded}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Instant, LocalDate}
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  id: String,
  userEori: Option[EORI],
  data: JsObject = Json.obj(),
  changePage: Option[String] = None,
  lastUpdated: Instant = Instant.now,
  fileUploadState: Option[FileUploadState] = None
) extends Answers {

  def isCreateSubmitted: Boolean = get(ClaimIdQuery).nonEmpty
  def isAmendSubmitted: Boolean  = get(AmendClaimIdQuery).nonEmpty

  def isImporterJourney: Boolean = get(ClaimantTypePage).contains(ClaimantType.Importer)

  def isAgentJourney: Boolean = get(ClaimantTypePage).contains(ClaimantType.Representative)

  def isSingleEntry: Boolean =
    get(NumberOfEntriesTypePage).map(_.numberOfEntriesType).contains(NumberOfEntriesType.Single)

  def isMultipleEntry: Boolean =
    get(NumberOfEntriesTypePage).map(_.numberOfEntriesType).contains(NumberOfEntriesType.Multiple)

  def isMultipleClaimReason: Boolean =
    get(ClaimReasonTypeMultiplePage).exists(_.size > 1)

  def customsRegulationType: Option[CustomsRegulationType] = {
    val ukCustomsRegulationStartDate: LocalDate = LocalDate.of(2021, 1, 1)
    get(EntryDetailsPage).map(_.EntryDate match {
      case date if date.isBefore(ukCustomsRegulationStartDate) => UnionsCustomsCodeRegulation
      case _                                                   => UKCustomsCodeRegulation
    })
  }

  def isCmaAllowed(implicit appConfig: FrontendAppConfig): Boolean =
    isSingleEntry &&
      dutyTypeTaxDetails.totalClaim >= appConfig.allowCmaThresholds.reclaimTotal &&
      get(EntryDetailsPage).exists(_.EntryDate.isAfter(appConfig.allowCmaThresholds.earliestEntryDate))

  def dutyTypeTaxDetails: DutyTypeTaxDetails =
    DutyTypeTaxDetails(
      Seq(
        getDutyTypeTaxList(ClaimRepaymentType.Customs, CustomsDutyPaidPage),
        getDutyTypeTaxList(ClaimRepaymentType.Vat, VATPaidPage),
        getDutyTypeTaxList(ClaimRepaymentType.Other, OtherDutiesPaidPage)
      )
    )

  private def getDutyTypeTaxList(
    repaymentType: ClaimRepaymentType,
    page: QuestionPage[RepaymentAmounts]
  ): DutyTypeTaxList = {

    val selectedDuties: Set[ClaimRepaymentType] = get(ClaimRepaymentTypePage).getOrElse(Set.empty)

    val dutyPaid: Option[String] = selectedDuties.contains(repaymentType) match {
      case true  => get(page).map(_.ActualPaidAmount)
      case false => Some("0.00")
    }

    val dutyDue: Option[String] = selectedDuties.contains(repaymentType) match {
      case true  => get(page).map(_.ShouldHavePaidAmount)
      case false => Some("0.00")
    }

    val paidAsBigDecimal = BigDecimal(dutyPaid.getOrElse("0.00")).setScale(2)
    val dueAsBigDecimal  = BigDecimal(dutyDue.getOrElse("0.00")).setScale(2)

    val owedAsString = (paidAsBigDecimal - dueAsBigDecimal).toString

    DutyTypeTaxList(repaymentType, paidAsBigDecimal.toString(), dueAsBigDecimal.toString, owedAsString)
  }

  def fileUploadPath: JsPath = JsPath \ "fileUploadState"
  def changePagePath: JsPath = JsPath \ "changePage"

  def dataPath: JsPath = JsPath \ "data"

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def removeFile(fileType: FileType): Try[UserAnswers] = Success(
    copy(fileUploadState = fileUploadState.map(_.remove(fileType)))
  )

  def remove(page: QuestionPage[_]*): Try[UserAnswers] = {
    var triedUserAnswers: Try[UserAnswers] = remove(page.head)
    page.tail.foreach(page => triedUserAnswers = triedUserAnswers.flatMap(_.remove(page)))
    triedUserAnswers
  }

  private def remove(page: QuestionPage[_]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, JsNull) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }

}

object UserAnswers {
  import services.UploadFile

  def apply(identification: Identification): UserAnswers =
    new UserAnswers(identification.identifier, identification.eori)

  implicit lazy val uploadReads: Reads[FileUploadState] =
    Reads {
      case jsObject: JsObject if (jsObject \ "acknowledged").isDefined =>
        FileUploaded.formatter.reads(jsObject)
      case jsObject: JsObject => services.UploadFile.formatter.reads(jsObject)
    }

  implicit lazy val uploadWrites: Writes[FileUploadState] =
    new Writes[FileUploadState] {

      override def writes(o: FileUploadState): JsValue =
        o match {
          case s: services.UploadFile =>
            UploadFile.formatter.writes(s)
          case s: services.FileUploaded =>
            FileUploaded.formatter.writes(s)
        }

    }

  implicit val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val formats: OFormat[UserAnswers]  = Json.format[UserAnswers]

}
