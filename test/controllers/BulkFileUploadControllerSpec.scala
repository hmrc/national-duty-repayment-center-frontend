package controllers

import base.SpecBase
import forms.BulkFileUploadFormProvider
import models.{NormalMode, BulkFileUpload, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.BulkFileUploadPage
import play.api.inject.bind
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.BulkFileUploadView

import scala.concurrent.Future

class BulkFileUploadControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val bulkFileUploadRoute = routes.BulkFileUploadController.onPageLoad().url

  val formProvider = new BulkFileUploadFormProvider()
  val form = formProvider()

  "BulkFileUpload Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, bulkFileUploadRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BulkFileUploadView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, bulkFileUploadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
