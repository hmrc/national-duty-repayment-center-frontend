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

package base

import java.time.{LocalDate, ZoneId, ZonedDateTime}

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.{Metrics, MetricsFilterImpl, MetricsImpl}
import config.FrontendAppConfig
import connectors.{UpscanInitiateConnector, UpscanInitiateRequest, UpscanInitiateResponse}
import controllers.actions._
import models.AmendCaseResponseType.FurtherInformation
import models._
import models.eis.EISAddress
import models.requests.{AmendClaimRequest, CreateClaimRequest, Identification, UploadRequest}
import navigation.{CreateNavigator, NavigatorBack}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{bind, Injector}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait SpecBase
    extends PlaySpec with GuiceOneAppPerSuite with TryValues with ScalaFutures with IntegrationPatience
    with MockitoSugar {

  val userAnswersId      = "id"
  val userIdentification = Identification(userAnswersId, None)

  def emptyUserAnswers = UserAnswers(userAnswersId, None, Json.obj())

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  val metrics: Metrics         = mock[Metrics]
  val registry: MetricRegistry = metrics.defaultRegistry
  val metricFilter             = mock[MetricsFilterImpl]
  when(metricFilter.registry).thenReturn(registry)

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  val upscanMock = mock[UpscanInitiateConnector]

  val uscanResponse =
    UpscanInitiateResponse(
      reference = "foo-bar-ref-new",
      uploadRequest =
        UploadRequest(href = "https://s3.bucket", fields = Map("callbackUrl" -> "https://foo.bar/callback-new"))
    )

  when(upscanMock.initiate(any[UpscanInitiateRequest])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(Future.successful(uscanResponse))

  val mockSessionRepository = mock[SessionRepository]

  val defaultBackLink         = NavigatorBack(Some(Call("GET", "/default-back-link")))
  def navBackLink(call: Call) = NavigatorBack(Some(call))
  val defaultNextPage         = Call("GET", "/default-next-page")

  val mockCreateNavigator = mock[CreateNavigator]
  when(mockCreateNavigator.previousPage(any(), any())).thenReturn(defaultBackLink)
  when(mockCreateNavigator.nextPage(any(), any())).thenReturn(defaultNextPage)

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None,
    createNavigator: CreateNavigator = mockCreateNavigator
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false, "auditing.enabled" -> false, "metrics.jvm" -> false).overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[UpscanInitiateConnector].toInstance(upscanMock),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[CreateNavigator].toInstance(createNavigator),
        bind[Metrics].to[MetricsImpl]
      )

  val claimDetails = ClaimDetails(
    FormType = FormType("01"),
    CustomRegulationType = CustomsRegulationType.UKCustomsCodeRegulation,
    ClaimedUnderArticle = None,
    ClaimedUnderRegulation = Some(UkRegulationType.Rejected),
    Claimant = ClaimantType.Representative,
    ClaimType = NumberOfEntriesType.Multiple,
    NoOfEntries = Some("10"),
    EntryDetails = EntryDetails(EPU = "123", EntryNumber = "123456Q", EntryDate = LocalDate.now()),
    ClaimReason = ClaimReasonType.Preference,
    ClaimDescription = ClaimDescription("this is a claim description"),
    DateReceived = LocalDate.of(2020, 8, 5),
    ClaimDate = LocalDate.of(2020, 8, 5),
    PayeeIndicator = WhomToPay.Importer,
    PaymentMethod = RepaymentType.BACS,
    DeclarantRefNumber = "P34567",
    DeclarantName = "DummyData"
  )

  val addressUk = Address(
    AddressLine1 = "line 1",
    AddressLine2 = Some("line 2"),
    City = "city",
    Region = Some("region"),
    Country = Country("GB", "United Kingdom"),
    PostalCode = Some("ZZ111ZZ"),
    auditRef = Some("audit-ref-a")
  )

  val addressInternational = Address(
    AddressLine1 = "line 1",
    AddressLine2 = Some("line 2"),
    City = "city",
    Region = Some("region"),
    Country = Country("FR", "France"),
    PostalCode = None,
    auditRef = Some("audit-ref-b")
  )

  val userDetails = UserDetails(
    IsVATRegistered = "true",
    EORI = EORI("GB123456789123456"),
    Name = "Joe Bloggs",
    Address = EISAddress(addressUk),
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

  val uploadedFiles = Seq(
    UploadedFile(
      "ref-123",
      downloadUrl = "/bucket/test1.jpeg",
      uploadTimestamp = ZonedDateTime.of(2020, 10, 10, 10, 10, 10, 0, ZoneId.of("UTC")),
      checksum = "f55a741917d512ab4c547ea97bdfdd8df72bed5fe51b6a248e0a5a0ae58061c8",
      fileName = "test1.jpeg",
      fileMimeType = "image/jpeg"
    )
  )

  val createClaimRequest = CreateClaimRequest(
    Content(
      claimDetails,
      AgentDetails = Some(userDetails),
      ImporterDetails = userDetails,
      BankDetails = Some(bankDetails),
      DutyTypeTaxDetails = dutyTypeTaxDetails,
      DocumentList = documentList
    ),
    uploadedFiles
  )

  val amendClaimRequest = AmendClaimRequest(
    AmendContent(
      CaseID = "Risk-2507",
      Description = "update request for Risk-2507",
      TypeOfAmendments = Seq(FurtherInformation)
    ),
    Nil
  )

  val amendJson = Json.obj(
    "Content" -> Json.obj(
      "CaseID"           -> "Risk-2507",
      "Description"      -> "update request for Risk-2507",
      "TypeOfAmendments" -> JsArray(Seq(Json.toJson(FurtherInformation.toString)))
    ),
    "uploadedFiles" -> JsArray()
  )

  val json = Json.obj(
    "Content" -> Json.obj(
      "ClaimDetails" -> Json.obj(
        "FormType"             -> "01",
        "CustomRegulationType" -> "02",
        "ClaimedUnderArticle"  -> "120",
        "Claimant"             -> "02",
        "ClaimType"            -> "02",
        "NoOfEntries"          -> "10",
        "EntryDetails"         -> Json.obj("EPU" -> "account name", "EntryNumber" -> "123456", "EntryDate" -> "2020-08-05"),
        "EntryNumber"          -> "123456A",
        "EntryDate"            -> "20200101",
        "ClaimReason"          -> "06",
        "ClaimDescription"     -> "this is a claim description",
        "DateReceived"         -> "20200805",
        "ClaimDate"            -> "20200805",
        "PayeeIndicator"       -> "01",
        "PaymentMethod"        -> "02"
      ),
      "AgentDetails" -> Json.obj(
        "VATNumber" -> "123456789",
        "EORI"      -> "GB123456789123456",
        "Name"      -> "Joe Bloggs",
        "Address" -> Json.obj(
          "AddressLine1" -> "line 1",
          "AddressLine2" -> "line 2",
          "City"         -> "city",
          "Region"       -> "region",
          "CountryCode"  -> "GB",
          "PostalCode"   -> "ZZ111ZZ"
        )
      ),
      "ImporterDetails" -> Json.obj(
        "VATNumber" -> "123456789",
        "EORI"      -> "GB123456789123456",
        "Name"      -> "Joe Bloggs",
        "Address" -> Json.obj(
          "AddressLine1" -> "line 1",
          "AddressLine2" -> "line 2",
          "City"         -> "city",
          "Region"       -> "region",
          "CountryCode"  -> "GB",
          "PostalCode"   -> "ZZ111ZZ"
        )
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
    )
  )

  def buildRequest(method: String, path: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path)
      .withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

}
