package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.ClaimIdQuery
import views.html.AmmendConfirmationView

class AmendConfirmationControllerSpec extends SpecBase {

  "AmmendConfirmation Controller" must {

    "return OK and the correct view for a GET" in {
      val claimId = "1"

      val answers = emptyUserAnswers
        .set(ClaimIdQuery, claimId).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, routes.AmmendConfirmationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AmmendConfirmationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(claimId)(fakeRequest, messages).toString

      application.stop()
    }
  }
}
