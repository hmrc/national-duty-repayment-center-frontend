package controllers

import base.SpecBase
import play.api.test.FakeRequest
import queries.ClaimIdQuery
import views.html.AmendConfirmationView
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AmendConfirmationControllerSpec extends SpecBase {
  "AmendConfirmation Controller" must {
    "return OK and the correct view for a GET" in {
      val claimId = "1"

      val answers = emptyUserAnswers
        .set(ClaimIdQuery, claimId).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, routes.AmendConfirmationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmendConfirmationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(claimId)(fakeRequest, messages).toString

      application.stop()
    }
  }
}
