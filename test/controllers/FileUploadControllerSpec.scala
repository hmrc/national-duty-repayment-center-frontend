package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.FileUploadView

class FileUploadControllerSpec extends SpecBase {

  "FileUpload Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.FileUploadController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[FileUploadView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(fakeRequest, messages).toString

      application.stop()
    }
  }
}
