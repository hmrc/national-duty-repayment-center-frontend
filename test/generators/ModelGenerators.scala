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
import java.util.{Calendar, Date}
import models.requests.CreateClaimRequest
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{choose, listOfN}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  val numberStringGen = (n: Int) => Gen.listOfN(n, Gen.numStr).map(_.mkString)

  val sf = new SimpleDateFormat("M/d/yyyy")
  val now = new Date
  val cal = Calendar.getInstance()
  cal.set(2013, 0, 1)

  val dates =
    Iterator.
      continually{
        val d = cal.getTime
        cal.add(Calendar.DAY_OF_YEAR, 1)
        sf.format(d)
      }.
      takeWhile(_ => cal.getTimeInMillis() <= now.getTime).
      toSeq


  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  //check this works
  def entryNumberGenerator: Gen[String] =
    for {
      firstPart <- listOfN(6, arbitrary[Int])
      secondPart <- listOfN(1,Gen.alphaNumStr)
    } yield (firstPart::secondPart).mkString

  implicit lazy val arbitraryBusinessType: Arbitrary[CustomRegulationType] =
    Arbitrary {
      Gen.oneOf(CustomRegulationType.values.toSeq)
    }

  /*implicit val arbitraryCreateClaimRequest: Arbitrary[CreateClaimRequest] = Arbitrary {
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
        Content(claimDetails,Some(agentDetails),importerDetails,Some(bankDetails),Some(dutyTypeTaxList),Seq[Some(documentList)]
      )
  }

  implicit val arbitraryClaimDetails: Arbitrary[ClaimDetails] = Arbitrary {
    for {
      formType <- "01"
      customRegulationType <- Gen.oneOf(Seq("01", "02"))
      claimedUnderArticle <- Gen.oneOf(Seq("117", "119", "120"))
      claimant <- Gen.oneOf(Seq("117", "119", "120"))
      claimType <- Gen.oneOf(Seq("01", "02"))
      noOfEntries <- Gen.choose(0, 999999999).toString
      epu <- numberStringGen(3)
      entryNumber <- entryNumberGenerator
      entryDate <- Gen.oneOf(dates)
      claimReason <- Gen.oneOf(Seq("01", "02", "03", "04", "05", "06", "07", "08", "09"))
      claimDescription <- arbitrary[String]
      dateReceived <- Gen.oneOf(dates)
      claimDate <- Gen.oneOf(dates)
      payeeIndicator <- Gen.oneOf(Seq("01", "02", "03"))
      paymentMethod <- Gen.oneOf(Seq("01", "02", "03"))
    } yield ClaimDetails(formType,
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
      dateReceived,
      claimDate,
      payeeIndicator,
      paymentMethod
    )
  }*/
}
