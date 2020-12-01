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

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators {

  self: Generators =>

  implicit lazy val arbitraryIndirectRepresentativeUserAnswersEntry: Arbitrary[(IndirectRepresentativePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IndirectRepresentativePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContactByEmailUserAnswersEntry: Arbitrary[(ContactByEmailPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactByEmailPage.type]
        value <- arbitrary[ContactByEmail].map(Json.toJson(_))
      } yield (page, value)
    }
  implicit lazy val arbitraryAdditionalFileUploadUserAnswersEntry: Arbitrary[(AdditionalFileUploadPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AdditionalFileUploadPage.type]
        value <- arbitrary[AdditionalFileUpload].map(Json.toJson(_))
      } yield (page, value)
    }



  implicit lazy val arbitraryBankDetailsUserAnswersEntry: Arbitrary[(BankDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BankDetailsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentImporterManualAddressUserAnswersEntry: Arbitrary[(AgentImporterManualAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentImporterManualAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImporterManualAddressUserAnswersEntry: Arbitrary[(ImporterManualAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImporterManualAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentImporterAddressUserAnswersEntry: Arbitrary[(AgentImporterAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentImporterAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImporterAddressUserAnswersEntry: Arbitrary[(ImporterAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImporterAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }



  implicit lazy val arbitraryOtherDutiesDueToHMRCUserAnswersEntry: Arbitrary[(OtherDutiesDueToHMRCPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[OtherDutiesDueToHMRCPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryOtherDutiesPaidUserAnswersEntry: Arbitrary[(OtherDutiesPaidPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[OtherDutiesPaidPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCustomsDutyDueToHMRCUserAnswersEntry: Arbitrary[(CustomsDutyDueToHMRCPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CustomsDutyDueToHMRCPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCustomsDutyPaidUserAnswersEntry: Arbitrary[(CustomsDutyPaidPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CustomsDutyPaidPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryVATDueToHMRCUserAnswersEntry: Arbitrary[(VATDueToHMRCPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[VATDueToHMRCPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryVATPaidUserAnswersEntry: Arbitrary[(VATPaidPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[VATPaidPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentImporterHasEORIUserAnswersEntry: Arbitrary[(AgentImporterHasEORIPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentImporterHasEORIPage.type]
        value <- arbitrary[AgentImporterHasEORI].map(Json.toJson(_))
        } yield (page, value)
    }
  
  implicit lazy val arbitraryIsImporterVatRegisteredUserAnswersEntry: Arbitrary[(IsImporterVatRegisteredPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IsImporterVatRegisteredPage.type]
        value <- arbitrary[IsImporterVatRegistered].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEnterAgentEORIUserAnswersEntry: Arbitrary[(EnterAgentEORIPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EnterAgentEORIPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhomToPayUserAnswersEntry: Arbitrary[(WhomToPayPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WhomToPayPage.type]
        value <- arbitrary[WhomToPay].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryRepaymentTypeUserAnswersEntry: Arbitrary[(RepaymentTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RepaymentTypePage.type]
        value <- arbitrary[RepaymentType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAgentNameImporterUserAnswersEntry: Arbitrary[(AgentNameImporterPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AgentNameImporterPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPhoneNumberUserAnswersEntry: Arbitrary[(PhoneNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PhoneNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryEmailAddressUserAnswersEntry: Arbitrary[(EmailAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EmailAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryContactTypeUserAnswersEntry: Arbitrary[(ContactTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContactTypePage.type]
        value <- arbitrary[ContactType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImporterNameUserAnswersEntry: Arbitrary[(ImporterNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImporterNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEvidenceSupportingDocsUserAnswersEntry: Arbitrary[(EvidenceSupportingDocsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EvidenceSupportingDocsPage.type]
        value <- arbitrary[EvidenceSupportingDocs].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimRepaymentTypeUserAnswersEntry: Arbitrary[(ClaimRepaymentTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimRepaymentTypePage.type]
        value <- arbitrary[ClaimRepaymentType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryReasonForOverpaymentUserAnswersEntry: Arbitrary[(ReasonForOverpaymentPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ReasonForOverpaymentPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatAreTheGoodsUserAnswersEntry: Arbitrary[(WhatAreTheGoodsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WhatAreTheGoodsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimReasonTypeUserAnswersEntry: Arbitrary[(ClaimReasonTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimReasonTypePage.type]
        value <- arbitrary[ClaimReasonType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimEntryDateUserAnswersEntry: Arbitrary[(ClaimEntryDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimEntryDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimEntryNumberUserAnswersEntry: Arbitrary[(ClaimEntryNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimEntryNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimEpuUserAnswersEntry: Arbitrary[(ClaimEpuPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimEpuPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyEntriesUserAnswersEntry: Arbitrary[(HowManyEntriesPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyEntriesPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNumberOfEntriesTypeUserAnswersEntry: Arbitrary[(NumberOfEntriesTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NumberOfEntriesTypePage.type]
        value <- arbitrary[NumberOfEntriesType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryArticleTypeUserAnswersEntry: Arbitrary[(ArticleTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ArticleTypePage.type]
        value <- arbitrary[ArticleType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCustomsRegulationTypeUserAnswersEntry: Arbitrary[(CustomsRegulationTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CustomsRegulationTypePage.type]
        value <- arbitrary[CustomsRegulationType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImporterClaimantVrnUserAnswersEntry: Arbitrary[(ImporterClaimantVrnPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImporterClaimantVrnPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsVatRegisteredUserAnswersEntry: Arbitrary[(IsVatRegisteredPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IsVatRegisteredPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImporterEoriUserAnswersEntry: Arbitrary[(ImporterEoriPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImporterEoriPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImporterHasEoriUserAnswersEntry: Arbitrary[(ImporterHasEoriPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImporterHasEoriPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimantTypeUserAnswersEntry: Arbitrary[(ClaimantTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimantTypePage.type]
        value <- arbitrary[ClaimantType].map(Json.toJson(_))
      } yield (page, value)
    }
}
