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

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.{Calendar, Date}

import models.requests.CreateClaimRequest
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{choose, listOfN}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  self: Generators =>

  val numberStringGen = (n: Int) => Gen.listOfN(n, Gen.numStr).map(_.mkString)

  implicit lazy val arbitraryEORI: Arbitrary[EORI] =
    Arbitrary {
      "GB" + Gen.listOfN(15, Gen.numStr).map(_.mkString)
    }

  implicit lazy val arbitrarySortCode: Arbitrary[SortCode] =
    Arbitrary {
      Gen.listOfN(6, Gen.numStr).map(_.mkString)
    }

  implicit lazy val arbitraryFormType: Arbitrary[FormType] =
    Arbitrary {
      arbitrary[String].map(FormType.apply)
    }

  implicit lazy val arbitraryCustomRegulationType: Arbitrary[CustomRegulationType] =
    Arbitrary {
      Gen.oneOf(CustomRegulationType.values.toSeq)
    }

  implicit lazy val arbitraryClaimedUnderArticle: Arbitrary[ClaimedUnderArticle] =
    Arbitrary {
      Gen.oneOf(ClaimedUnderArticle.values.toSeq)
    }

  implicit lazy val arbitraryClaimant: Arbitrary[Claimant] =
    Arbitrary {
      Gen.oneOf(Claimant.values.toSeq)
    }

  implicit lazy val arbitraryClaimType: Arbitrary[ClaimType] =
    Arbitrary {
      Gen.oneOf(ClaimType.values.toSeq)
    }

  implicit lazy val arbitraryNoOfEntries: Arbitrary[NoOfEntries] =
    Arbitrary {
      arbitrary[String].map(NoOfEntries.apply)
    }

  implicit lazy val arbitraryEPU: Arbitrary[EPU] =
    Arbitrary {
      arbitrary[String].map(EPU.apply)
    }

  implicit lazy val arbitraryUserName: Arbitrary[UserName] =
    Arbitrary {
      arbitrary[String].map(UserName.apply)
    }

  implicit lazy val arbitraryEntryNumber: Arbitrary[EntryNumber] =
    Arbitrary {
      arbitrary[String].map(EntryNumber.apply)
    }

  implicit lazy val arbitraryClaimReason: Arbitrary[ClaimReason] =
    Arbitrary {
      Gen.oneOf(ClaimReason.values.toSeq)
    }

  implicit lazy val arbitraryClaimDescription: Arbitrary[ClaimDescription] =
    Arbitrary {
      arbitrary[String].map(ClaimDescription.apply)
    }

  implicit lazy val arbitraryPayeeIndicator: Arbitrary[PayeeIndicator] =
    Arbitrary {
      Gen.oneOf(PayeeIndicator.values.toSeq)
    }

  implicit lazy val arbitraryPaymentMethod: Arbitrary[PaymentMethod] =
    Arbitrary {
      Gen.oneOf(PaymentMethod.values.toSeq)
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
      claimDetails <- arbitrary[ClaimDetails]
      agentDetails <- arbitrary[UserDetails]
      importerDetails <- arbitrary[UserDetails]
      bankDetails <- arbitrary[AllBankDetails]
      dutyTypeTaxList <- arbitrary[Seq[DutyTypeTaxList]]
      documentList <- arbitrary[Seq[DocumentList]]
    } yield
      CreateClaimRequest(
        acknowledgementReference = "X123456",
        originatingSystem = "Digital",
        applicationType = "NDRC",
        Content(claimDetails,Some(agentDetails),importerDetails,Some(bankDetails),Some(dutyTypeTaxList),Some(documentList))
      )
  }

  implicit val arbitraryClaimDetails: Arbitrary[ClaimDetails] = Arbitrary {
    for {
      formType <- arbitrary[FormType]
      customRegulationType <- arbitrary[CustomRegulationType]
      claimedUnderArticle <- arbitrary[ClaimedUnderArticle]
      claimant <- arbitrary[Claimant]
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
    } yield ClaimDetails(formType,
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

  implicit val arbitraryAddress: Arbitrary[Address] = Arbitrary {
    for {
      addressLine1 <- arbitrary[String]
      addressLine2 <- arbitrary[String]
      city:String <- arbitrary[String]
      region <- arbitrary[String]
      countryCode <- arbitrary[String]
      postalCode <- arbitrary[String]
      telephoneNumber <- arbitrary[String]
      emailAddress <- arbitrary[String]
    } yield Address(addressLine1,
      Some(addressLine2),
      city,
      region,
      countryCode,
      Some(postalCode),
      Some(telephoneNumber),
      Some(emailAddress)
    )
  }

  implicit val arbitraryUserDetails: Arbitrary[UserDetails] = Arbitrary {
    for {
      vatNumber <- arbitrary[String]
      eori <- arbitrary[EORI]
      name <- arbitrary[UserName]
      address <- arbitrary[Address]
    } yield UserDetails(Some(vatNumber),
      eori,
      name,
      address
    )
  }
}

