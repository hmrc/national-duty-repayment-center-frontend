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
    arbitrary[(customsDutyPaidPage.type, JsValue)] ::
    arbitrary[(VATDueToHMRCPage.type, JsValue)] ::
    arbitrary[(VATPaidPage.type, JsValue)] ::
    arbitrary[(AgentImporterHasEORIPage.type, JsValue)] ::
    arbitrary[(IsImporterVatRegisteredPage.type, JsValue)] ::
    arbitrary[(EnterAgentEORIPage.type, JsValue)] ::
    arbitrary[(WhomToPayPage.type, JsValue)] ::
    arbitrary[(RepaymentTypePage.type, JsValue)] ::
    arbitrary[(AgentNameImporterPage.type, JsValue)] ::
    arbitrary[(PhoneNumberPage.type, JsValue)] ::
    arbitrary[(EmailAddressPage.type, JsValue)] ::
    arbitrary[(ContactTypePage.type, JsValue)] ::
    arbitrary[(ImporterNamePage.type, JsValue)] ::
    arbitrary[(EvidenceSupportingDocsPage.type, JsValue)] ::
    arbitrary[(ClaimRepaymentTypePage.type, JsValue)] ::
    arbitrary[(ReasonForOverpaymentPage.type, JsValue)] ::
    arbitrary[(WhatAreTheGoodsPage.type, JsValue)] ::
    arbitrary[(ClaimReasonTypePage.type, JsValue)] ::
    arbitrary[(ClaimEntryDatePage.type, JsValue)] ::
    arbitrary[(ClaimEntryNumberPage.type, JsValue)] ::
    arbitrary[(ClaimEpuPage.type, JsValue)] ::
    arbitrary[(HowManyEntriesPage.type, JsValue)] ::
    arbitrary[(NumberOfEntriesTypePage.type, JsValue)] ::
    arbitrary[(ArticleTypePage.type, JsValue)] ::
    arbitrary[(CustomsRegulationTypePage.type, JsValue)] ::
    arbitrary[(ImporterClaimantVrnPage.type, JsValue)] ::
    arbitrary[(IsVatRegisteredPage.type, JsValue)] ::
    arbitrary[(ImporterEoriPage.type, JsValue)] ::
    arbitrary[(ImporterHasEoriPage.type, JsValue)] ::
    arbitrary[(ClaimantTypePage.type, JsValue)] ::
    Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id      <- nonEmptyString
        data    <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers (
        id = id,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}
