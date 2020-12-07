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

import java.time.LocalDate

import models._
import base.SpecBase
import org.scalatest.MustMatchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsSuccess, Json}

class CreateClaimRequestSpec extends SpecBase with MustMatchers with MockitoSugar {

  "CreateClaimRequest" must {
    "serialise and deserialise to / from a claim period" in {

      val claimDetails = ClaimDetails(
        FormType = FormType("01"),
        CustomRegulationType = CustomsRegulationType.UKCustomsCodeRegulation,
        ClaimedUnderArticle = ArticleType.OverPaymentOfDutyOrVAT,
        Claimant = ClaimantType.Representative,
        ClaimType = NumberOfEntriesType.Multiple,
        NoOfEntries = Some(NoOfEntries("10")),
        EntryDetails = EntryDetails(EPU = "123", EntryNumber = "123456Q", EntryDate = LocalDate.of(2020, 8, 5)),
        ClaimReason = ClaimReasonType.Preference,
        ClaimDescription = ClaimDescription("this is a claim description"),
        DateReceived = LocalDate.of(2020,8,5),
        ClaimDate = LocalDate.of(2020,8,5),
        PayeeIndicator = WhomToPay.Importer,
        PaymentMethod = RepaymentType.BACS,
      )

      val address = Address(AddressLine1 = "line 1",
        AddressLine2 = Some("line 2"),
        City = "city",
        Region = "region",
        CountryCode = "GB",
        postCode = Some("ZZ111ZZ")
      )

      val userDetails = UserDetails(VATNumber = Some(VRN("123456789")),
        EORI = EORI("GB123456789123456"),
        Name = UserName("Joe Bloggs"),
        Address = address,
        TelephoneNumber = Some("12345678"),
        EmailAddress = Some("example@example.com")
      )

      val bankDetails = AllBankDetails(
        AgentBankDetails = Some(BankDetails("account name", "123456", "12345678")),
        ImporterBankDetails = Some(BankDetails("account name", "123456", "12345678"))
      )

      val dutyTypeTaxList = Seq(
        DutyTypeTaxList(ClaimRepaymentType.Customs, Some("100.00"), Some("50.00"), Some("50.00")),
        DutyTypeTaxList(ClaimRepaymentType.Vat, Some("100.00"), Some("50.00"), Some("50.00")),
        DutyTypeTaxList(ClaimRepaymentType.Other, Some("100.00"), Some("50.00"), Some("50.00"))
      )

      val documentList = Seq(
        DocumentList(EvidenceSupportingDocs.CopyOfC88, Some(DocumentDescription("this is a copy of c88"))),
        DocumentList(EvidenceSupportingDocs.Invoice, Some(DocumentDescription("this is an invoice"))),
        DocumentList(EvidenceSupportingDocs.PackingList, Some(DocumentDescription("this is a packing list"))),
      )

      val dutyTypeTaxDetails = DutyTypeTaxDetails(dutyTypeTaxList)

      val createClaimRequest = CreateClaimRequest(
        Content(claimDetails,
          AgentDetails = Some(userDetails),
          ImporterDetails = userDetails,
          BankDetails = Some(bankDetails),
          DutyTypeTaxDetails = dutyTypeTaxDetails,
          DocumentList = documentList)
        )

      val json = Json.obj(
        "Content" -> Json.obj(
          "ClaimDetails" -> Json.obj(
            "FormType" -> "01",
            "CustomRegulationType" -> "02",
            "ClaimedUnderArticle" -> "117",
            "Claimant" -> "02",
            "ClaimType" -> "02",
            "NoOfEntries" -> "10",
            "EntryDetails" -> Json.obj(
              "EPU" -> "123",
              "EntryNumber" -> "123456Q",
              "EntryDate" -> "2020-08-05"
            ),
            "ClaimReason" -> "05",
            "ClaimDescription" -> "this is a claim description",
            "DateReceived" -> "20200805",
            "ClaimDate" -> "20200805",
            "PayeeIndicator" -> "01",
            "PaymentMethod" -> "01",
            ),
          "AgentDetails" -> Json.obj(
          "VATNumber" -> "123456789",
          "EORI" -> "GB123456789123456",
          "Name" -> "Joe Bloggs",
          "Address" -> Json.obj(
            "AddressLine1" -> "line 1",
            "AddressLine2" -> "line 2",
            "City" -> "city",
            "Region" -> "region",
            "CountryCode" -> "GB",
            "postCode" -> "ZZ111ZZ"
            ),
            "TelephoneNumber" -> "12345678",
            "EmailAddress" -> "example@example.com"
          ),
          "ImporterDetails" -> Json.obj(
            "VATNumber" -> "123456789",
            "EORI" -> "GB123456789123456",
            "Name" -> "Joe Bloggs",
            "Address" -> Json.obj(
              "AddressLine1" -> "line 1",
              "AddressLine2" -> "line 2",
              "City" -> "city",
              "Region" -> "region",
              "CountryCode" -> "GB",
              "postCode" -> "ZZ111ZZ"
            ),
            "TelephoneNumber" -> "12345678",
            "EmailAddress" -> "example@example.com"
          ),
          "BankDetails" -> Json.obj(
            "ImporterBankDetails" -> Json.obj(
            "AccountName" -> "account name",
            "SortCode" -> "123456",
            "AccountNumber" -> "12345678"
            ),
            "AgentBankDetails" -> Json.obj(
              "AccountName" -> "account name",
              "SortCode" -> "123456",
              "AccountNumber" -> "12345678"
            )
          ),
          "DutyTypeTaxDetails" -> Json.obj(
          "DutyTypeTaxList" -> Json.arr(
            Json.obj(
              "Type" -> "01",
              "PaidAmount" -> "100.00",
              "DueAmount" -> "50.00",
              "ClaimAmount" -> "50.00"
            ),
            Json.obj(
              "Type" -> "02",
              "PaidAmount" -> "100.00",
              "DueAmount" -> "50.00",
              "ClaimAmount" -> "50.00"
            ),
            Json.obj(
              "Type" -> "03",
              "PaidAmount" -> "100.00",
              "DueAmount" -> "50.00",
              "ClaimAmount" -> "50.00"
            )
          )
        ),
        "DocumentList" -> Json.arr(
          Json.obj(
            "Type" -> "03",
            "Description" -> "this is a copy of c88"
          ),
          Json.obj(
            "Type" -> "01",
            "Description" -> "this is an invoice"
          ),
          Json.obj(
            "Type" -> "04",
            "Description" -> "this is a packing list"
          )
        )
        )
      )

      Json.toJson(createClaimRequest) mustEqual json
      json.validate[CreateClaimRequest] mustEqual JsSuccess(createClaimRequest)
    }
  }
}
