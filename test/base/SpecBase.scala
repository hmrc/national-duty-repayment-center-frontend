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

package base

import java.time.LocalDate

import config.FrontendAppConfig
import controllers.actions._
import models.requests.CreateClaimRequest
import models.{AccountName, AccountNumber, AcknowledgementReference, Address, AllBankDetails, ApplicationType, BankDetails, ClaimAmount, ClaimDescription, ClaimDetails, ClaimReason, ClaimType, Claimant, ClaimedUnderArticle, Content, CustomRegulationType, DocumentDescription, DocumentList, DocumentUploadType, DueAmount, DutyType, DutyTypeTaxDetails, DutyTypeTaxList, EORI, EPU, EntryNumber, FormType, NoOfEntries, OriginatingSystem, PaidAmount, PayeeIndicator, PaymentMethod, SortCode, UserAnswers, UserDetails, UserName, VRN}
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.json.Json
import play.api.test.FakeRequest

trait SpecBase extends PlaySpec with GuiceOneAppPerSuite with TryValues with ScalaFutures with IntegrationPatience {

  val userAnswersId = "id"

  def emptyUserAnswers = UserAnswers(userAnswersId, Json.obj())

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  val claimDetails = ClaimDetails(
    FormType = FormType("01"),
    CustomRegulationType = CustomRegulationType.UKCustomsCodeRegulation,
    ClaimedUnderArticle = ClaimedUnderArticle.Equity,
    Claimant = Claimant.RepresentativeOfTheImporter,
    ClaimType = ClaimType.Multiple,
    NoOfEntries = Some(NoOfEntries("10")),
    EPU = EPU("777"),
    EntryNumber = EntryNumber("123456A"),
    EntryDate = LocalDate.of(2020,1,1),
    ClaimReason = ClaimReason.Preference,
    ClaimDescription = ClaimDescription("this is a claim description"),
    DateReceived = LocalDate.of(2020,8,5),
    ClaimDate = LocalDate.of(2020,8,5),
    PayeeIndicator = PayeeIndicator.Importer,
    PaymentMethod = PaymentMethod.BACS,
  )

  val address = Address(AddressLine1 = "line 1",
    AddressLine2 = Some("line 2"),
    City = "city",
    Region = "region",
    CountryCode = "GB",
    PostalCode = Some("ZZ111ZZ"),
    TelephoneNumber = Some("12345678"),
    EmailAddress = Some("example@example.com")
  )

  val userDetails = UserDetails(VATNumber = Some(VRN("123456789")),
    EORI = EORI("GB123456789123456"),
    Name = UserName("Joe Bloggs"),
    Address = address
  )

  val bankDetails = AllBankDetails(
    AgentBankDetails = BankDetails(AccountName("account name"), SortCode("123456"), AccountNumber("12345678")),
    ImporterBankDetails = BankDetails(AccountName("account name"), SortCode("123456"), AccountNumber("12345678"))
  )


  val dutyTypeTaxList = Seq(
    DutyTypeTaxList(DutyType.Customs, Some(PaidAmount("100.00")), Some(DueAmount("50.00")), Some(ClaimAmount("50.00"))),
    DutyTypeTaxList(DutyType.Vat, Some(PaidAmount("100.00")), Some(DueAmount("50.00")), Some(ClaimAmount("50.00"))),
    DutyTypeTaxList(DutyType.Other, Some(PaidAmount("100.00")), Some(DueAmount("50.00")), Some(ClaimAmount("50.00")))
  )

  val documentList = Seq(
    DocumentList(DocumentUploadType.CopyOfC88, Some(DocumentDescription("this is a copy of c88"))),
    DocumentList(DocumentUploadType.Invoice, Some(DocumentDescription("this is an invoice"))),
    DocumentList(DocumentUploadType.PackingList, Some(DocumentDescription("this is a packing list"))),
  )

  val dutyTypeTaxDetails = DutyTypeTaxDetails(dutyTypeTaxList)

  val createClaimRequest = CreateClaimRequest(
    AcknowledgementReference("123456"),
    ApplicationType("NDRC"),
    OriginatingSystem("Digital"),
    Content(claimDetails,
      AgentDetails = Some(userDetails),
      ImporterDetails = userDetails,
      BankDetails = Some(bankDetails),
      DutyTypeTaxDetails = dutyTypeTaxDetails,
      DocumentList = documentList)
  )

  val json = Json.obj(
    "AcknowledgementReference" -> "123456",
    "ApplicationType" -> "NDRC",
    "OriginatingSystem" -> "Digital",
    "Content" -> Json.obj(
      "ClaimDetails" -> Json.obj(
        "FormType" -> "01",
        "CustomRegulationType" -> "02",
        "ClaimedUnderArticle" -> "120",
        "Claimant" -> "02",
        "ClaimType" -> "02",
        "NoOfEntries" -> "10",
        "EPU" -> "777",
        "EntryNumber" -> "123456A",
        "EntryDate" -> "20200101",
        "ClaimReason" -> "06",
        "ClaimDescription" -> "this is a claim description",
        "DateReceived" -> "20200805",
        "ClaimDate" -> "20200805",
        "PayeeIndicator" -> "01",
        "PaymentMethod" -> "02",
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
          "PostalCode" -> "ZZ111ZZ",
          "TelephoneNumber" -> "12345678",
          "EmailAddress" -> "example@example.com"
        )
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
          "PostalCode" -> "ZZ111ZZ",
          "TelephoneNumber" -> "12345678",
          "EmailAddress" -> "example@example.com"
        )
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


}
