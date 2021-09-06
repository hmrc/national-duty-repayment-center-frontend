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

package controllers

import java.time.ZonedDateTime

import base.SpecBase
import data.TestData.{testClaimRepaymentType, testEntryDetailsPreJan2021, testRepaymentAmounts}
import forms.NumberOfEntriesTypeFormProvider
import models.FileType.Bulk
import models.NumberOfEntriesType.Single
import models.RepaymentType.CMA
import models.WhomToPay.Importer
import models.{Entries, FileUpload, FileUploads, NumberOfEntriesType, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FileUploaded
import views.html.NumberOfEntriesTypeView

import scala.concurrent.Future

class NumberOfEntriesTypeControllerSpec extends SpecBase with MockitoSugar {

  lazy val numberOfEntriesTypeRoute = routes.NumberOfEntriesTypeController.onPageLoad().url

  val formProvider = new NumberOfEntriesTypeFormProvider()
  val form         = formProvider()

  "NumberOfEntriesType Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, numberOfEntriesTypeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[NumberOfEntriesTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userIdentification).set(
        NumberOfEntriesTypePage,
        Entries(NumberOfEntriesType.Multiple, Some("2"))
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, numberOfEntriesTypeRoute)

      val view = application.injector.instanceOf[NumberOfEntriesTypeView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(Entries(NumberOfEntriesType.Multiple, Some("2"))), defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, numberOfEntriesTypeRoute)
          .withFormUrlEncodedBody(("value", NumberOfEntriesType.options(form).head.value.get))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "remove answers when number of entries changes" in {

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val fileUploadedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf",
              fileType = Some(Bulk)
            )
          )
        ),
        acknowledged = true
      )

      val completeUserAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadedState))
        .set(EntryDetailsPage, testEntryDetailsPreJan2021)
        .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
        .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
        .flatMap(_.set(VATPaidPage, testRepaymentAmounts))
        .flatMap(_.set(OtherDutiesPaidPage, testRepaymentAmounts))
        .flatMap(_.set(WhomToPayPage, Importer))
        .flatMap(_.set(RepaymentTypePage, CMA))
        .get

      val application =
        applicationBuilder(userAnswers = Some(completeUserAnswers))
          .build()

      val request =
        FakeRequest(POST, numberOfEntriesTypeRoute)
          .withFormUrlEncodedBody(("value", "02"), ("entries", "100"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()

      persistedAnswers.getValue.get(EntryDetailsPage) mustBe None
      persistedAnswers.getValue.get(ClaimRepaymentTypePage) mustBe None
      persistedAnswers.getValue.get(CustomsDutyPaidPage) mustBe None
      persistedAnswers.getValue.get(VATPaidPage) mustBe None
      persistedAnswers.getValue.get(OtherDutiesPaidPage) mustBe None
      persistedAnswers.getValue.get(WhomToPayPage) mustBe None
      persistedAnswers.getValue.get(RepaymentTypePage) mustBe None

      persistedAnswers.getValue.fileUploadState mustBe Some(FileUploaded(FileUploads(Seq.empty), acknowledged = true))
    }

    "not remove answers when number of entries doesn't changes" in {

      val persistedAnswers: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(persistedAnswers.capture())) thenReturn Future.successful(true)

      val fileUploadedState = FileUploaded(
        FileUploads(files =
          Seq(
            FileUpload.Accepted(
              1,
              "foo-bar-ref-1",
              "https://bucketName.s3.eu-west-2.amazonaws.com?1235676",
              ZonedDateTime.parse("2018-04-24T09:30:00Z"),
              "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
              "test.pdf",
              "application/pdf",
              fileType = Some(Bulk)
            )
          )
        ),
        acknowledged = true
      )

      val completeUserAnswers = emptyUserAnswers.copy(fileUploadState = Some(fileUploadedState))
        .set(EntryDetailsPage, testEntryDetailsPreJan2021)
        .flatMap(_.set(NumberOfEntriesTypePage, Entries(Single, None)))
        .flatMap(_.set(ClaimRepaymentTypePage, testClaimRepaymentType))
        .flatMap(_.set(CustomsDutyPaidPage, testRepaymentAmounts))
        .flatMap(_.set(VATPaidPage, testRepaymentAmounts))
        .flatMap(_.set(OtherDutiesPaidPage, testRepaymentAmounts))
        .flatMap(_.set(WhomToPayPage, Importer))
        .flatMap(_.set(RepaymentTypePage, CMA))
        .get

      val application =
        applicationBuilder(userAnswers = Some(completeUserAnswers))
          .build()

      val request =
        FakeRequest(POST, numberOfEntriesTypeRoute)
          .withFormUrlEncodedBody(("value", Single.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()

      persistedAnswers.getValue mustBe completeUserAnswers
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, numberOfEntriesTypeRoute)
          .withFormUrlEncodedBody(("entries", "invalid value"))

      val boundForm = form.bind(Map("entries" -> "invalid value"))

      val view = application.injector.instanceOf[NumberOfEntriesTypeView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, numberOfEntriesTypeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, numberOfEntriesTypeRoute)
          .withFormUrlEncodedBody(("value", NumberOfEntriesType.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
