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

import models.{AcknowledgementReference, ApplicationType, ClaimDetails, Content, OriginatingSystem, UserAnswers}
import pages.{ArticleTypePage, ClaimantTypePage, CustomsRegulationTypePage, NumberOfEntriesTypePage}
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
    def getRestaurants(userAnswers: UserAnswers): Option[Seq[RestaurantInfo]] = for {
      numberOfRestaurants <- userAnswers.get(NumberOfRestaurantsPage)
      restaurantsJson <- if (numberOfRestaurants > Constants.largeBusinessCutoff) Some(Seq.empty) else userAnswers.get(AllRestaurantJsonQuery)
      if restaurantsJson.forall(_.value.get("restaurantRegisteredWithLocalAuthority").contains(JsTrue))
      restaurants <- if (numberOfRestaurants > Constants.largeBusinessCutoff) Some(Seq.empty) else userAnswers.get(AllRestaurantsQuery)
      if restaurants.nonEmpty || numberOfRestaurants > Constants.largeBusinessCutoff
    } yield restaurants

    def getClaimDetails(userAnswers: UserAnswers): Option[ClaimDetails] = for {
      formType <- "01"
      customRegulationType <- userAnswers.get(CustomsRegulationTypePage)
      claimedUnderArticle <- userAnswers.get(ArticleTypePage)
      claimant <- userAnswers.get(ClaimantTypePage)
      claimType <- userAnswers.get(NumberOfEntriesTypePage)
      noOfEntries <- userAnswers.get(NumberOfEntriesTypePage)

    }

    def getContent(userAnswers: UserAnswers): Option[Content] = for {
      claimDetails <- getClaimDetails,
      agentDetails <-

    }


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

