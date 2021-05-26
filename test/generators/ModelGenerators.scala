/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.EmailAndPhoneNumber
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ModelGenerators {

  self: Generators =>

  implicit lazy val arbitraryAmendCaseResponseType: Arbitrary[AmendCaseResponseType] =
    Arbitrary {
      Gen.oneOf(AmendCaseResponseType.values.toSeq)
    }

  implicit lazy val arbitraryBulkFileUpload: Arbitrary[BulkFileUpload] =
    Arbitrary {
      Gen.oneOf(BulkFileUpload.values.toSeq)
    }

  //telephoneNumber <- Gen.option(Gen.listOfN(11, Gen.numStr).map(_.mkString))
  //emailAddress <- Gen.option(self.stringsWithMaxLength(85))

  implicit lazy val arbitraryAgentImporterHasEORI: Arbitrary[AgentImporterHasEORI] =
    Arbitrary {
      Gen.oneOf(AgentImporterHasEORI.values)
    }
  
  implicit lazy val arbitraryIsImporterVatRegistered: Arbitrary[IsImporterVatRegistered] =
    Arbitrary {
      Gen.oneOf(IsImporterVatRegistered.values)
    }

  implicit lazy val arbitraryWhomToPay: Arbitrary[WhomToPay] =
    Arbitrary {
      Gen.oneOf(WhomToPay.values)
    }

  implicit lazy val arbitraryRepaymentType: Arbitrary[RepaymentType] =
    Arbitrary {
      Gen.oneOf(RepaymentType.values)
    }

  implicit lazy val arbitraryRepresentativeDeclarantAndBusinessName: Arbitrary[RepresentativeDeclarantAndBusinessName] =
    Arbitrary {
      for {
        dec <- arbitrary[String]
        agentName <- arbitrary[String]
      } yield RepresentativeDeclarantAndBusinessName(dec, agentName)
    }

  implicit lazy val arbitraryEvidenceSupportingDocs: Arbitrary[EvidenceSupportingDocs] =
    Arbitrary {
      Gen.oneOf(EvidenceSupportingDocs.values)
    }

  implicit lazy val arbitraryClaimRepaymentType: Arbitrary[ClaimRepaymentType] =
    Arbitrary {
      Gen.oneOf(ClaimRepaymentType.values)
    }

  implicit lazy val arbitraryClaimReasonType: Arbitrary[ClaimReasonType] =
    Arbitrary {
      Gen.oneOf(ClaimReasonType.values)
    }

  implicit lazy val arbitraryNumberOfEntriesType: Arbitrary[NumberOfEntriesType] =
    Arbitrary {
      Gen.oneOf(NumberOfEntriesType.values)
    }

  implicit lazy val arbitraryEmailAndPhoneNumber: Arbitrary[EmailAndPhoneNumber] =
    Arbitrary {
      for {
        email <- arbitrary[String]
        phone <- arbitrary[String]
      } yield EmailAndPhoneNumber(setIsContactProvided,
        Some(email),
        Some(phone)
      )
    }

  implicit lazy val setIsContactProvided: Set[IsContactProvided] =
    Set (
      IsContactProvided.Email, IsContactProvided.Phone
    )

  implicit lazy val arbitraryEntriesType: Arbitrary[Entries] =
    Arbitrary {
      for {
        numberOfEntriesType <- arbitrary[NumberOfEntriesType]
        entries <- arbitrary[String]
      } yield Entries(numberOfEntriesType,
        Some(entries)
      )
    }

  implicit lazy val arbitraryArticleType: Arbitrary[ArticleType] =
    Arbitrary {
      Gen.oneOf(ArticleType.values)
    }

  implicit lazy val arbitraryUkRegulationType: Arbitrary[UkRegulationType] =
    Arbitrary {
      Gen.oneOf(UkRegulationType.values)
    }

  implicit lazy val arbitraryCustomsRegulationType: Arbitrary[CustomsRegulationType] =
    Arbitrary {
      Gen.oneOf(CustomsRegulationType.values)
    }

  implicit lazy val arbitraryClaimantType: Arbitrary[ClaimantType] =
    Arbitrary {
      Gen.oneOf(ClaimantType.values)
    }

  implicit lazy val arbitraryCreateOrAmendCase: Arbitrary[CreateOrAmendCase] =
    Arbitrary {
      Gen.oneOf(CreateOrAmendCase.values)
    }

  implicit val arbitraryBankDetails: Arbitrary[BankDetails] = Arbitrary {
    for {
      accountName <- arbitrary[String]
      sortCode <- arbitrary[String]
      accountNumber <- arbitrary[String]
    } yield BankDetails(accountName,
      sortCode,
      accountNumber
    )
  }

  implicit val arbitraryRepaymentAmounts: Arbitrary[RepaymentAmounts] = Arbitrary {
    for {
      paid <- Gen.choose(99999.99, 9999999.99)
      due <- Gen.choose(0.10, 9999.99)
    } yield RepaymentAmounts(paid.toString, due.toString)
  }

  implicit lazy val arbitraryNoOfEntries: Arbitrary[NoOfEntries] =
    Arbitrary {
      Gen.listOfN(2, Gen.numStr).map(_.mkString).map(NoOfEntries.apply)
    }

  implicit lazy val userName: Arbitrary[UserName] =
    Arbitrary {
      self.stringsWithMaxLength(512).map(UserName.apply)
    }

  implicit lazy val arbitraryEori: Arbitrary[EORI] =
    Arbitrary {
      self.stringsWithMaxLength(17).map(EORI.apply)
    }

  implicit lazy val arbitraryEntryDetails: Arbitrary[EntryDetails] =
    Arbitrary {
      for {
        epu <- arbitrary[String]
        entryNumber <- arbitrary[String]
        entryDate <- datesBetween(LocalDate.now, LocalDate.now.plusYears(10))
      } yield EntryDetails(epu,
        entryNumber,
        entryDate
      )
    }

  implicit lazy val arbitraryName: Arbitrary[Name] =
    Arbitrary {
      for {
        firstName <- self.stringsWithMaxLength(512)
        lastName <- self.stringsWithMaxLength(512)
      } yield Name(firstName, lastName)
    }

  implicit lazy val arbitraryDoYouOwnTheGoods: Arbitrary[DoYouOwnTheGoods] =
    Arbitrary {
      Gen.oneOf(DoYouOwnTheGoods.values)
    }

  implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary {
    for {
      addressLine1 <- self.stringsWithMaxLength(128)
      addressLine2 <- Gen.option(self.stringsWithMaxLength(128))
      city <- self.stringsWithMaxLength(64)
      region <- self.stringsWithMaxLength(64)
      countryCode <- Gen.pick(2, 'A' to 'Z')
      postCode <- self.stringsWithMinAndMaxLength(2,10)
    } yield Address(addressLine1,
      addressLine2,
      city,
      Some(region),
      countryCode.mkString,
      postCode
    )
  }

  implicit lazy val arbitraryClaimDescription: Arbitrary[ClaimDescription] =
    Arbitrary {
      self.stringsWithMaxLength(750).map(ClaimDescription.apply)
    }

  implicit lazy val arbitraryIsVATRegistered: Arbitrary[IsVATRegistered] =
    Arbitrary {
      Gen.oneOf(IsVATRegistered.values)
    }
}

  /*self: Generators =>

  lazy val dutyAmount: Gen[String] = Gen.listOfN(14, Gen.numStr).map(_.mkString)

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



  implicit lazy val arbitraryUserName: Arbitrary[UserName] =
    Arbitrary {
      self.stringsWithMaxLength(512).map(UserName.apply)
    }



  implicit lazy val arbitraryClaimReason: Arbitrary[ClaimReason] =
    Arbitrary {
      Gen.oneOf(ClaimReason.values)
    }



  implicit lazy val arbitraryPayeeIndicator: Arbitrary[PayeeIndicator] =
    Arbitrary {
      Gen.oneOf(PayeeIndicator.values)
    }

  implicit lazy val arbitraryPaymentMethod: Arbitrary[PaymentMethod] =
    Arbitrary {
      Gen.oneOf(PaymentMethod.values)
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
      claimDetails <- arbitrary[ClaimDetails]
      agentDetails <- arbitrary[UserDetails]
      importerDetails <- arbitrary[UserDetails]
      bankDetails <- arbitrary[AllBankDetails]
      dutyTypeTaxDetails <- arbitrary[DutyTypeTaxDetails]
      documentList <- arbitrary[DocumentList]
    } yield
      CreateClaimRequest(
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

