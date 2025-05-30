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

package controllers

import base.SpecBase
import forms.EntryDetailsFormProvider
import models.{Entries, EntryDetails, NumberOfEntriesType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import pages.{EntryDetailsPage, NumberOfEntriesTypePage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.EntryDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class EntryDetailsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new EntryDetailsFormProvider()
  val form         = formProvider()

  val validDateAnswer: LocalDate = LocalDate.parse("0900-01-01")

  lazy val entryDetailsRoute = routes.EntryDetailsController.onPageLoad().url

  private val userAnswers = UserAnswers(
    userAnswersId,
    None,
    Json.obj(
      EntryDetailsPage.toString -> Json.obj("EPU" -> "123", "EntryNumber" -> "123456Q", "EntryDate" -> "09000101")
    )
  )

  "EntryDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userIdentification).set(
        NumberOfEntriesTypePage,
        Entries(NumberOfEntriesType.Multiple, Some("2"))
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, entryDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[EntryDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, defaultBackLink, false, false)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers =
        Some(userAnswers.set(NumberOfEntriesTypePage, Entries(NumberOfEntriesType.Multiple, Some("2"))).success.value)
      ).build()

      val request = FakeRequest(GET, entryDetailsRoute)

      val view = application.injector.instanceOf[EntryDetailsView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(EntryDetails("123", "123456Q", validDateAnswer)), defaultBackLink, false, false)(
          request,
          messages
        ).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val dateAnswer: LocalDate = LocalDate.parse("2020-01-01")

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      val request =
        FakeRequest(POST, entryDetailsRoute)
          .withFormUrlEncodedBody(
            ("EPU", "123"),
            ("EntryNumber", "123456Q"),
            ("EntryDate.day", dateAnswer.getDayOfMonth.toString),
            ("EntryDate.month", dateAnswer.getMonthValue.toString),
            ("EntryDate.year", dateAnswer.getYear.toString)
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual defaultNextPage.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userIdentification).set(
        NumberOfEntriesTypePage,
        Entries(NumberOfEntriesType.Multiple, Some("2"))
      ).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, entryDetailsRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[EntryDetailsView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, defaultBackLink, false, false)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, entryDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, entryDetailsRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
