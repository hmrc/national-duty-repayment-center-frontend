package views

import controllers.routes
import forms.customsDutyPaidFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.customsDutyPaidView

class customsDutyPaidViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "customsDutyPaid"

  val form = new customsDutyPaidFormProvider()()

  "customsDutyPaidView view" must {

    val view = viewFor[customsDutyPaidView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like stringPage(form, applyView, messageKeyPrefix, routes.customsDutyPaidController.onSubmit(NormalMode).url)
  }
}
