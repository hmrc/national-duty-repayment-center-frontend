package views

import controllers.routes
import forms.CustomsDutyDueFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.CustomsDutyDueView

class CustomsDutyDueViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "customsDutyDue"

  val form = new CustomsDutyDueFormProvider()()

  "CustomsDutyDueView view" must {

    val view = viewFor[CustomsDutyDueView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like stringPage(form, applyView, messageKeyPrefix, routes.CustomsDutyDueController.onSubmit(NormalMode).url)
  }
}
