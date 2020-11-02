package views

import views.behaviours.ViewBehaviours
import views.html.confirmationView

class confirmationViewSpec extends ViewBehaviours {

  "confirmation view" must {

    val view = viewFor[confirmationView](Some(emptyUserAnswers))

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView, "confirmation")

    behave like pageWithBackLink(applyView)
  }
}
