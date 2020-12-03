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
import javax.swing.text.View
import pages._
import models._
import org.jsoup.nodes.{Document, Element}
import views.behaviours.ViewBehaviours
import views.html.ProofOfAuthorityView

import scala.reflect.ClassTag

class NavigatorSpec extends SpecBase with ViewBehaviours {

  val navigator = new Navigator
  //  def instanceOf[T <: AnyRef](implicit classTag: ClassTag[T]): T = injector.instanceOf[T]
  //
  //  private val page: ProofOfAuthorityView = instanceOf[ProofOfAuthorityView]
  //
  //  private def createView(mode: Mode = NormalMode)(
  //  ): ProofOfAuthorityView = page

  val view = ProofOfAuthorityPage

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

      "go to BankDetails page after proofOfAuthority page once the representative has uploaded their proof of authority" in {

        print("sssss" + view)

        view.getElementById("govuk-link") mustBe (routes.BankDetailsController.onPageLoad(NormalMode))

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
