package views

import views.behaviours.ViewBehaviours
import views.html.RepaymentAmountSummaryView

class RepaymentAmountSummaryViewSpec extends ViewBehaviours {

  "RepaymentAmountSummary view" must {

    val view = viewFor[RepaymentAmountSummaryView](Some(emptyUserAnswers))

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView, "repaymentAmountSummary")

    behave like pageWithBackLink(applyView)
  }
}
