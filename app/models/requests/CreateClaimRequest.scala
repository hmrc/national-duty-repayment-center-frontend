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

package models.requests

import java.time.LocalDate

import models.{AcknowledgementReference, ApplicationType, ClaimDetails, Content, FormType, OriginatingSystem, UserAnswers, UserDetails}
import pages.{ArticleTypePage, ClaimEntryDatePage, ClaimEntryNumberPage, ClaimEpuPage, ClaimReasonTypePage, ClaimantTypePage, CustomsRegulationTypePage, HowManyEntriesPage, NumberOfEntriesTypePage, ReasonForOverpaymentPage, RepaymentTypePage, WhomToPayPage}
import play.api.libs.json.{Json, OFormat}

final case class CreateClaimRequest(
                                     AcknowledgementReference: AcknowledgementReference,
                                     ApplicationType: ApplicationType,
                                     OriginatingSystem: OriginatingSystem,
                                     Content: Content
                                   )

object CreateClaimRequest {
  implicit val formats: OFormat[CreateClaimRequest] = Json.format[CreateClaimRequest]

  def buildValidClaimRequest(userAnswers: UserAnswers): Option[CreateClaimRequest] = {

    def getClaimDetails(userAnswers: UserAnswers): Option[ClaimDetails] = for {
      customRegulationType <- userAnswers.get(CustomsRegulationTypePage)
      claimedUnderArticle <- userAnswers.get(ArticleTypePage)
      claimant <- userAnswers.get(ClaimantTypePage)
      claimType <- userAnswers.get(NumberOfEntriesTypePage)
      noOfEntries <- Some(userAnswers.get(HowManyEntriesPage))
      epu <- userAnswers.get(ClaimEpuPage)
      entryNumber <- userAnswers.get(ClaimEntryNumberPage)
      entryDate <- userAnswers.get(ClaimEntryDatePage)
      claimReason <- userAnswers.get(ClaimReasonTypePage)
      claimDescription <- userAnswers.get(ReasonForOverpaymentPage)
      payeeIndicator <- userAnswers.get(WhomToPayPage)
      paymentMethod <- userAnswers.get(RepaymentTypePage)
    } yield ClaimDetails(FormType("01"),
      customRegulationType,
      claimedUnderArticle,
      claimant,
      claimType,
      noOfEntries,
      epu,
      entryNumber,
      entryDate,
      claimReason,
      claimDescription,
      LocalDate.now(),
      LocalDate.now(),
      payeeIndicator,
      paymentMethod)

    def getAgentUserDetails(userAnswers: UserAnswers): Option[UserDetails] = for {
      eori <- userAnswers.get(Agent)
    }

    def getContent(userAnswers: UserAnswers): Option[Content] = for {
      claimDetails <- getClaimDetails(userAnswers)
      agentDetails <- ???
      importerDetails <- ???
      bankDetails <- ???
      dutyTypeTaxDetails <- ???
      documentList <- ???
    } yield Content(
      claimDetails,
      agentDetails,
      importerDetails,
      bankDetails,
      dutyTypeTaxDetails,
      documentList
    )


    for {
      acknowledgementReference <- AcknowledgementReference("123456").value
      applicationType <- ApplicationType("NDRC").value
      originatingSystem <- OriginatingSystem("Digital").value
      content <- getContent(userAnswers)
    } yield CreateClaimRequest(
      acknowledgementReference,
      applicationType,
      originatingSystem,
      content
    )
  }
}

