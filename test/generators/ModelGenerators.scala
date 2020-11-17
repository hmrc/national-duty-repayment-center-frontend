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

import java.time.LocalDate
import models.requests.CreateClaimRequest
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryAdditionalFileUpload: Arbitrary[AdditionalFileUpload] =
    Arbitrary {
      Gen.oneOf(AdditionalFileUpload.values.toSeq)
    }

  implicit lazy val arbitraryAgentImporterHasEORI: Arbitrary[AgentImporterHasEORI] =
    Arbitrary {
      Gen.oneOf(AgentImporterHasEORI.values.toSeq)
    }
  
  implicit lazy val arbitraryIsImporterVatRegistered: Arbitrary[IsImporterVatRegistered] =
    Arbitrary {
      Gen.oneOf(IsImporterVatRegistered.values.toSeq)
    }

  implicit lazy val arbitraryWhomToPay: Arbitrary[WhomToPay] =
    Arbitrary {
      Gen.oneOf(WhomToPay.values.toSeq)
    }

  implicit lazy val arbitraryRepaymentType: Arbitrary[RepaymentType] =
    Arbitrary {
      Gen.oneOf(RepaymentType.values.toSeq)
    }

  implicit lazy val arbitraryContactType: Arbitrary[ContactType] =
    Arbitrary {
      Gen.oneOf(ContactType.values.toSeq)
    }

  implicit lazy val arbitraryEvidenceSupportingDocs: Arbitrary[EvidenceSupportingDocs] =
    Arbitrary {
      Gen.oneOf(EvidenceSupportingDocs.values.toSeq)
    }

  implicit lazy val arbitraryClaimRepaymentType: Arbitrary[ClaimRepaymentType] =
    Arbitrary {
      Gen.oneOf(ClaimRepaymentType.values.toSeq)
    }

  implicit lazy val arbitraryClaimReasonType: Arbitrary[ClaimReasonType] =
    Arbitrary {
      Gen.oneOf(ClaimReasonType.values.toSeq)
    }

  implicit lazy val arbitraryNumberOfEntriesType: Arbitrary[NumberOfEntriesType] =
    Arbitrary {
      Gen.oneOf(NumberOfEntriesType.values.toSeq)
    }

  implicit lazy val arbitraryArticleType: Arbitrary[ArticleType] =
    Arbitrary {
      Gen.oneOf(ArticleType.values.toSeq)
    }

  implicit lazy val arbitraryCustomsRegulationType: Arbitrary[CustomsRegulationType] =
    Arbitrary {
      Gen.oneOf(CustomsRegulationType.values.toSeq)
    }

  implicit lazy val arbitraryClaimantType: Arbitrary[ClaimantType] =
    Arbitrary {
      Gen.oneOf(ClaimantType.values.toSeq)
    }
}

  /*self: Generators =>

  lazy val dutyAmount: Gen[String] = Gen.listOfN(14, Gen.numStr).map(_.mkString)

  implicit lazy val arbitraryAcknowledgementReference: Arbitrary[AcknowledgementReference] =
    Arbitrary {
      self.stringsWithMaxLength(32).map(AcknowledgementReference.apply)
    }

  implicit lazy val arbitraryOriginatingSystem: Arbitrary[OriginatingSystem] =
    Arbitrary {
      self.stringsWithMaxLength(32).map(OriginatingSystem.apply)
    }

  implicit lazy val arbitraryApplicationType: Arbitrary[ApplicationType] =
    Arbitrary {
      self.stringsWithMaxLength(32).map(ApplicationType.apply)
    }

  implicit lazy val arbitrarySortCode: Arbitrary[SortCode] =
    Arbitrary {
      Gen.listOfN(6, Gen.numStr).map(_.mkString).map(SortCode.apply)
    }

  implicit lazy val arbitraryAccountName: Arbitrary[AccountName] =
    Arbitrary {
      self.stringsWithMaxLength(40).map(AccountName.apply)
    }

  implicit lazy val arbitraryAccountNumber: Arbitrary[AccountNumber] =
    Arbitrary {
      Gen.listOfN(8, Gen.numStr).map(_.mkString).map(AccountNumber.apply)
    }

  implicit lazy val arbitraryFormType: Arbitrary[FormType] =
    Arbitrary {
      self.stringsWithMaxLength(2).map(FormType.apply)
    }

  implicit lazy val arbitraryCustomRegulationType: Arbitrary[CustomRegulationType] =
    Arbitrary {
      Gen.oneOf(CustomRegulationType.values)
    }

  implicit lazy val arbitraryDocumentUploadType: Arbitrary[DocumentUploadType] =
    Arbitrary {
      Gen.oneOf(DocumentUploadType.values)
    }

  implicit lazy val arbitraryDocumentDescription: Arbitrary[DocumentDescription] =
    Arbitrary {
      self.stringsWithMaxLength(1500).map(DocumentDescription.apply)
    }

  implicit lazy val arbitraryEori: Arbitrary[EORI] =
    Arbitrary {
      self.stringsWithMaxLength(17).map(EORI.apply)
    }

  implicit lazy val arbitraryVrn: Arbitrary[VRN] =
    Arbitrary {
      Gen.listOfN(9, Gen.numStr).map(_.mkString).map(VRN.apply)
    }

  implicit lazy val arbitraryClaimedUnderArticle: Arbitrary[ClaimedUnderArticle] =
    Arbitrary {
      Gen.oneOf(ClaimedUnderArticle.values)
    }

  implicit lazy val arbitraryDutyType: Arbitrary[DutyType] =
    Arbitrary {
      Gen.oneOf(DutyType.values)
    }

  //TODO: generate string with decimal place
  implicit lazy val arbitraryClaimAmount: Arbitrary[ClaimAmount] =
    Arbitrary {
      dutyAmount.map(ClaimAmount.apply)
    }

  implicit lazy val arbitraryPaidAmount: Arbitrary[PaidAmount] =
    Arbitrary {
      dutyAmount.map(PaidAmount.apply)
    }

  implicit lazy val arbitraryDueAmount: Arbitrary[DueAmount] =
    Arbitrary {
      dutyAmount.map(DueAmount.apply)
    }

  implicit lazy val arbitraryClaimType: Arbitrary[ClaimType] =
    Arbitrary {
      Gen.oneOf(ClaimType.values)
    }

  implicit lazy val arbitraryNoOfEntries: Arbitrary[NoOfEntries] =
    Arbitrary {
      Gen.listOfN(2, Gen.numStr).map(_.mkString).map(NoOfEntries.apply)
    }

  implicit lazy val arbitraryEPU: Arbitrary[EPU] =
    Arbitrary {
      self.stringsWithMinAndMaxLength(3,3).map(EPU.apply)
    }

  implicit lazy val arbitraryUserName: Arbitrary[UserName] =
    Arbitrary {
      self.stringsWithMaxLength(512).map(UserName.apply)
    }

  implicit lazy val arbitraryEntryNumber: Arbitrary[EntryNumber] =
    Arbitrary {
      self.stringsWithMinAndMaxLength(7,7).map(EntryNumber.apply)
    }

  implicit lazy val arbitraryClaimReason: Arbitrary[ClaimReason] =
    Arbitrary {
      Gen.oneOf(ClaimReason.values)
    }

  implicit lazy val arbitraryClaimDescription: Arbitrary[ClaimDescription] =
    Arbitrary {
      self.stringsWithMaxLength(1500).map(ClaimDescription.apply)
    }

  implicit lazy val arbitraryPayeeIndicator: Arbitrary[PayeeIndicator] =
    Arbitrary {
      Gen.oneOf(PayeeIndicator.values)
    }

  implicit lazy val arbitraryPaymentMethod: Arbitrary[PaymentMethod] =
    Arbitrary {
      Gen.oneOf(PaymentMethod.values)
    }

  implicit val arbitraryBankDetails: Arbitrary[BankDetails] = Arbitrary {
    for {
      accountName <- arbitrary[AccountName]
      sortCode <- arbitrary[SortCode]
      accountNumber <- arbitrary[AccountNumber]
    } yield BankDetails(accountName,
      sortCode,
      accountNumber
    )
  }

  implicit val arbitraryAllBankDetails: Arbitrary[AllBankDetails] = Arbitrary {
    for {
      agentBankDetails <- arbitrary[BankDetails]
      importerBankDetails <- arbitrary[BankDetails]
    } yield AllBankDetails(agentBankDetails,
      importerBankDetails
    )
  }

  implicit val arbitraryCreateClaimRequest: Arbitrary[CreateClaimRequest] = Arbitrary {
    for {
      acknowledgementReference <- arbitrary[AcknowledgementReference]
      originatingSystem <- arbitrary[OriginatingSystem]
      applicationType <- arbitrary[ApplicationType]
      claimDetails <- arbitrary[ClaimDetails]
      agentDetails <- arbitrary[UserDetails]
      importerDetails <- arbitrary[UserDetails]
      bankDetails <- arbitrary[AllBankDetails]
      dutyTypeTaxDetails <- arbitrary[DutyTypeTaxDetails]
      documentList <- arbitrary[DocumentList]
    } yield
      CreateClaimRequest(
        acknowledgementReference = acknowledgementReference,
        applicationType = applicationType,
        originatingSystem = originatingSystem,
        Content(claimDetails,Some(agentDetails),importerDetails,Some(bankDetails), dutyTypeTaxDetails,Seq(documentList))
      )
  }

  implicit val arbitraryClaimDetails: Arbitrary[ClaimDetails] = Arbitrary {
    for {
      formType <- arbitrary[FormType]
      customRegulationType <- arbitrary[CustomRegulationType]
      claimedUnderArticle <- arbitrary[ClaimedUnderArticle]
      claimantType <- arbitrary[ClaimantType]
      claimType <- arbitrary[ClaimType]
      noOfEntries <- arbitrary[NoOfEntries]
      epu <- arbitrary[EPU]
      entryNumber <- arbitrary[EntryNumber]
      entryDate <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2020, 1, 1))
      claimReason <- arbitrary[ClaimReason]
      claimDescription <- arbitrary[ClaimDescription]
      dateReceived <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2020, 1, 1))
      claimDate <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2020, 1, 1))
      payeeIndicator <- arbitrary[PayeeIndicator]
      paymentMethod <- arbitrary[PaymentMethod]
    } yield {
      println("formType " + formType)
      ClaimDetails(formType,
        customRegulationType,
        claimedUnderArticle,
        claimant,
        claimType,
        Some(noOfEntries),
        epu,
        entryNumber,
        entryDate,
        claimReason,
        claimDescription,
        dateReceived,
        claimDate,
        payeeIndicator,
        paymentMethod
      )
    }
  }

  implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary {
    for {
      addressLine1 <- self.stringsWithMaxLength(128)
      addressLine2 <- Gen.option(self.stringsWithMaxLength(128))
      city <- self.stringsWithMaxLength(64)
      region <- self.stringsWithMaxLength(64)
      countryCode <- Gen.pick(2, 'A' to 'Z')
      postalCode <- Gen.option(arbitrary[String])
      telephoneNumber <- Gen.option(Gen.listOfN(11, Gen.numStr).map(_.mkString))
      emailAddress <- Gen.option(self.stringsWithMaxLength(85))
    } yield Address(addressLine1,
      addressLine2,
      city,
      region,
      countryCode.mkString,
      postalCode,
      telephoneNumber,
      emailAddress
    )
  }

  implicit val arbitraryUserDetails: Arbitrary[UserDetails] = Arbitrary {
    for {
      vatNumber <- arbitrary[VRN]
      eori <- arbitrary[EORI]
      name <- arbitrary[UserName]
      address <- arbitrary[Address]
    } yield UserDetails(Some(vatNumber),
      eori,
      name,
      address
    )
  }

  implicit val arbitraryDutyTypeTaxList: Arbitrary[DutyTypeTaxList] = Arbitrary {
    for {
      dutyType <- arbitrary[DutyType]
      paidAmount <- arbitrary[PaidAmount]
      dueAmount <- arbitrary[DueAmount]
      claimAmount <- arbitrary[ClaimAmount]
    } yield DutyTypeTaxList(
      dutyType,
      Some(paidAmount),
      Some(dueAmount),
      Some(claimAmount)
    )
  }

  implicit val arbitraryDocumentList: Arbitrary[DocumentList] = Arbitrary {
    for {
      documentType <- arbitrary[DocumentUploadType]
      documentDescription <- arbitrary[DocumentDescription]
    } yield DocumentList(
      documentType,
      Some(documentDescription)
    )
  }
}*/

