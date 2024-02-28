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

package models.requests

import models._
import models.eis.QuoteFormatter
import pages.{AmendCaseResponseTypePage, FurtherInformationPage, ReferenceNumberPage}
import play.api.libs.json.{Json, OFormat}

import javax.inject.Inject

final case class AmendClaimRequest(Content: AmendContent, uploadedFiles: Seq[UploadedFile])

object AmendClaimRequest {
  implicit val formats: OFormat[AmendClaimRequest] = Json.format[AmendClaimRequest]
}

class AmendClaimBuilder @Inject() (quoteFormatter: QuoteFormatter) {

  def buildValidAmendRequest(userAnswers: UserAnswers): Option[AmendClaimRequest] = {

    def getFurtherInformation(userAnswers: UserAnswers): Option[String] = {
      val formattedDescription =
        userAnswers.get(AmendCaseResponseTypePage).map(_.contains(AmendCaseResponseType.FurtherInformation)) match {
          case Some(true) => userAnswers.get(FurtherInformationPage).map(info => s"${quoteFormatter.format(info)}")
          case _          => Some("Files Uploaded")
        }
      formattedDescription.map(description =>
        s"[EORINumber=${userAnswers.userEori.map(_.value).getOrElse("GBPR")}]\n\n$description"
      )
    }

    def getAmendCaseResponseType(userAnswers: UserAnswers): Seq[AmendCaseResponseType] =
      userAnswers.get(AmendCaseResponseTypePage).getOrElse(Nil).toSeq

    def getContent(userAnswers: UserAnswers): Option[AmendContent] = for {
      referenceNumber    <- userAnswers.get(ReferenceNumberPage)
      furtherInformation <- getFurtherInformation(userAnswers)
    } yield AmendContent(referenceNumber, furtherInformation, getAmendCaseResponseType(userAnswers))

    for {
      content <- getContent(userAnswers)
    } yield AmendClaimRequest(
      content,
      if (!hasSupportingDocs(userAnswers)) Nil
      else userAnswers.fileUploadState.map(_.fileUploads.toUploadedFiles).getOrElse(Nil)
    )
  }

  def hasSupportingDocs(userAnswers: UserAnswers): Boolean =
    userAnswers.get(AmendCaseResponseTypePage) match {
      case Some(s) => s.contains(AmendCaseResponseType.SupportingDocuments)
      case _       => false
    }

}
