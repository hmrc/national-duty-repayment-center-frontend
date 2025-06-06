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

package models.requests

import java.time.LocalDate
import base.SpecBase
import models._
import models.eis.EISAddress
import org.scalatest.matchers.must.Matchers
import org.mockito.MockitoSugar
import play.api.libs.json.{JsSuccess, Json}

class CreateClaimRequestSpec extends SpecBase with Matchers with MockitoSugar {

  "CreateClaimRequest" must {
    "serialise and deserialise to / from a claim period" in {

      val claimDetails = ClaimDetails(
        FormType = FormType("01"),
        CustomRegulationType = CustomsRegulationType.UKCustomsCodeRegulation,
        ClaimedUnderArticle = None,
        ClaimedUnderRegulation = Some(UkRegulationType.Rejected),
        Claimant = ClaimantType.Representative,
        ClaimType = NumberOfEntriesType.Multiple,
        NoOfEntries = Some("10"),
        EntryDetails = EntryDetails(EPU = "123", EntryNumber = "123456Q", EntryDate = LocalDate.of(2020, 8, 5)),
        ClaimReason = ClaimReasonType.Preference,
        ClaimDescription = ClaimDescription("this is a claim description"),
        DateReceived = LocalDate.of(2020, 8, 5),
        ClaimDate = LocalDate.of(2020, 8, 5),
        PayeeIndicator = WhomToPay.Importer,
        PaymentMethod = RepaymentType.BACS,
        DeclarantRefNumber = "12345",
        DeclarantName = "DummyData"
      )

      val address = Address(
        AddressLine1 = "line 1",
        AddressLine2 = Some("line 2"),
        City = "city",
        Region = Some("region"),
        Country = Country("GB", "United Kingdom"),
        PostalCode = Some("ZZ111ZZ")
      )

      val userDetails = UserDetails(
        IsVATRegistered = "true",
        EORI = EORI("GB123456789123456"),
        Name = "Joe Bloggs",
        Address = EISAddress(address),
        TelephoneNumber = Some("12345678"),
        EmailAddress = Some("example@example.com")
      )

      val bankDetails = AllBankDetails(
        AgentBankDetails = Some(BankDetails("account name", "123456", "12345678")),
        ImporterBankDetails = Some(BankDetails("account name", "123456", "12345678"))
      )

      val dutyTypeTaxList = Seq(
        DutyTypeTaxList(ClaimRepaymentType.Customs, "100.00", "50.00", "50.00"),
        DutyTypeTaxList(ClaimRepaymentType.Vat, "100.00", "50.00", "50.00"),
        DutyTypeTaxList(ClaimRepaymentType.Other, "100.00", "50.00", "50.00")
      )

      val documentList = Seq(
        DocumentList(EvidenceSupportingDocs.CopyOfC88, Some(DocumentDescription("this is a copy of c88"))),
        DocumentList(EvidenceSupportingDocs.Invoice, Some(DocumentDescription("this is an invoice"))),
        DocumentList(EvidenceSupportingDocs.PackingList, Some(DocumentDescription("this is a packing list")))
      )

      val dutyTypeTaxDetails = DutyTypeTaxDetails(dutyTypeTaxList)

      val createClaimRequest = CreateClaimRequest(
        Content(
          claimDetails,
          AgentDetails = Some(userDetails),
          ImporterDetails = userDetails,
          BankDetails = Some(bankDetails),
          DutyTypeTaxDetails = dutyTypeTaxDetails,
          DocumentList = documentList
        ),
        Nil
      )

      val json = Json.obj(
        "Content" -> Json.obj(
          "ClaimDetails" -> Json.obj(
            "FormType"               -> "01",
            "CustomRegulationType"   -> "02",
            "ClaimedUnderRegulation" -> "051",
            "Claimant"               -> "02",
            "ClaimType"              -> "02",
            "NoOfEntries"            -> "10",
            "EntryDetails"           -> Json.obj("EPU" -> "123", "EntryNumber" -> "123456Q", "EntryDate" -> "20200805"),
            "ClaimReason"            -> "06",
            "ClaimDescription"       -> "this is a claim description",
            "DateReceived"           -> "20200805",
            "ClaimDate"              -> "20200805",
            "PayeeIndicator"         -> "01",
            "PaymentMethod"          -> "02",
            "DeclarantRefNumber"     -> "12345",
            "DeclarantName"          -> "DummyData"
          ),
          "AgentDetails" -> Json.obj(
            "IsVATRegistered" -> "true",
            "EORI"            -> "GB123456789123456",
            "Name"            -> "Joe Bloggs",
            "Address" -> Json.obj(
              "AddressLine1" -> "line 1",
              "AddressLine2" -> "line 2",
              "City"         -> "city",
              "Region"       -> "region",
              "CountryCode"  -> "GB",
              "PostalCode"   -> "ZZ111ZZ"
            ),
            "TelephoneNumber" -> "12345678",
            "EmailAddress"    -> "example@example.com"
          ),
          "ImporterDetails" -> Json.obj(
            "IsVATRegistered" -> "true",
            "EORI"            -> "GB123456789123456",
            "Name"            -> "Joe Bloggs",
            "Address" -> Json.obj(
              "AddressLine1" -> "line 1",
              "AddressLine2" -> "line 2",
              "City"         -> "city",
              "Region"       -> "region",
              "CountryCode"  -> "GB",
              "PostalCode"   -> "ZZ111ZZ"
            ),
            "TelephoneNumber" -> "12345678",
            "EmailAddress"    -> "example@example.com"
          ),
          "BankDetails" -> Json.obj(
            "ImporterBankDetails" -> Json.obj(
              "AccountName"   -> "account name",
              "SortCode"      -> "123456",
              "AccountNumber" -> "12345678"
            ),
            "AgentBankDetails" -> Json.obj(
              "AccountName"   -> "account name",
              "SortCode"      -> "123456",
              "AccountNumber" -> "12345678"
            )
          ),
          "DutyTypeTaxDetails" -> Json.obj(
            "DutyTypeTaxList" -> Json.arr(
              Json.obj("Type" -> "01", "PaidAmount" -> "100.00", "DueAmount" -> "50.00", "ClaimAmount" -> "50.00"),
              Json.obj("Type" -> "02", "PaidAmount" -> "100.00", "DueAmount" -> "50.00", "ClaimAmount" -> "50.00"),
              Json.obj("Type" -> "03", "PaidAmount" -> "100.00", "DueAmount" -> "50.00", "ClaimAmount" -> "50.00")
            )
          ),
          "DocumentList" -> Json.arr(
            Json.obj("Type" -> "03", "Description" -> "this is a copy of c88"),
            Json.obj("Type" -> "01", "Description" -> "this is an invoice"),
            Json.obj("Type" -> "04", "Description" -> "this is a packing list")
          )
        ),
        "uploadedFiles" -> Array.empty[UploadedFile]
      )

      Json.toJson(createClaimRequest) mustEqual json
      json.validate[CreateClaimRequest] mustEqual JsSuccess(createClaimRequest)
    }

  }
}
