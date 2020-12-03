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

package navigation

import base.SpecBase
import controllers.routes
import pages._
import models._
import views.behaviours.ViewBehaviours


class NavigatorSpec extends SpecBase with ViewBehaviours {

  val navigator = new Navigator


  "Navigator" when {

    "in Normal mode" must {

      "go to Index from a page that doesn't exist in the route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "go to IndirectRepresentative after WhomToPay page when the claimant is representative and has selected representative to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(WhomToPayPage, WhomToPay.Representative).success.value

        navigator.nextPage(WhomToPayPage, NormalMode, answers)
          .mustBe(routes.IndirectRepresentativeController.onPageLoad(NormalMode))
      }

      "go to BankDetails page after WhomToPay page when the claimant is representative and has selected importer to be paid" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(WhomToPayPage, WhomToPay.Importer).success.value

        navigator.nextPage(WhomToPayPage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))
      }

      "go to BankDetails page after IndirectRepresentative page when the claimant is representative and has selected yes" in {
        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(IndirectRepresentativePage, true).success.value

        navigator.nextPage(IndirectRepresentativePage, NormalMode, answers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))
      }

      "go to proofOfAuthority page after after IndirectRepresentative page when the claimant is representative and has selected no" in {

        val answers =
          emptyUserAnswers
            .set(ClaimantTypePage, ClaimantType.Representative).success.value
            .set(IndirectRepresentativePage, false).success.value

        navigator.nextPage(IndirectRepresentativePage, NormalMode, answers)
          .mustBe(routes.ProofOfAuthorityController.onPageLoad)
      }

      "go to CheckYourAnswers page after the claimant page once the representative has uploaded their proof of authority" in {

        val answers =
          emptyUserAnswers
        navigator.nextPage(ProofOfAuthorityPage, NormalMode, answers)
          .mustBe(routes.IndexController.onPageLoad)

      }

      "go to EntryDetails page after ArticleType page " in {
        navigator.nextPage(ArticleTypePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.EntryDetailsController.onPageLoad(NormalMode))
      }

      "go to ClaimReasonType page after EntryDetails page " in {
        navigator.nextPage(EntryDetailsPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.ClaimReasonTypeController.onPageLoad(NormalMode))
      }

    }


    "in Check mode" must {

      "go to CheckYourAnswers from a page that doesn't exist in the edit route map" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
