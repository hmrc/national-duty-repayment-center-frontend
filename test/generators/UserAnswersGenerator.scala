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

package generators

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(AmendCaseSendInformationPage.type, JsValue)] ::
      arbitrary[(AmendCaseResponseTypePage.type, JsValue)] ::
      arbitrary[(FurtherInformationPage.type, JsValue)] ::
      arbitrary[(ReferenceNumberPage.type, JsValue)] ::
      arbitrary[(BulkFileUploadPage.type, JsValue)] ::
      arbitrary[(IndirectRepresentativePage.type, JsValue)] ::
      arbitrary[(BankDetailsPage.type, JsValue)] ::
      arbitrary[(AgentImporterAddressPage.type, JsValue)] ::
      arbitrary[(ImporterAddressPage.type, JsValue)] ::
      arbitrary[(OtherDutiesPaidPage.type, JsValue)] ::
      arbitrary[(CustomsDutyPaidPage.type, JsValue)] ::
      arbitrary[(VATPaidPage.type, JsValue)] ::
      arbitrary[(AgentImporterHasEORIPage.type, JsValue)] ::
      arbitrary[(IsImporterVatRegisteredPage.type, JsValue)] ::
      arbitrary[(EnterAgentEORIPage.type, JsValue)] ::
      arbitrary[(WhomToPayPage.type, JsValue)] ::
      arbitrary[(RepaymentTypePage.type, JsValue)] ::
      arbitrary[(RepresentativeDeclarantAndBusinessNamePage.type, JsValue)] ::
      arbitrary[(EmailAddressAndPhoneNumberPage.type, JsValue)] ::
      arbitrary[(ImporterNamePage.type, JsValue)] ::
      arbitrary[(EvidenceSupportingDocsPage.type, JsValue)] ::
      arbitrary[(ClaimRepaymentTypePage.type, JsValue)] ::
      arbitrary[(ReasonForOverpaymentPage.type, JsValue)] ::
      arbitrary[(ClaimReasonTypePage.type, JsValue)] ::
      arbitrary[(EntryDetailsPage.type, JsValue)] ::
      arbitrary[(NumberOfEntriesTypePage.type, JsValue)] ::
      arbitrary[(ArticleTypePage.type, JsValue)] ::
      arbitrary[(IsVATRegisteredPage.type, JsValue)] ::
      arbitrary[(ImporterEoriPage.type, JsValue)] ::
      arbitrary[(ImporterHasEoriPage.type, JsValue)] ::
      arbitrary[(ClaimantTypePage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id <- nonEmptyString
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        id = id,
        userEori = None,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }

}
