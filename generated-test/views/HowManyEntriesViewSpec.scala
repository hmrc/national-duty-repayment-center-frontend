package views

import controllers.routes
import forms.HowManyEntriesFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.HowManyEntriesView

class HowManyEntriesViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "howManyEntries"

  val form = new HowManyEntriesFormProvider()()

  "HowManyEntriesView view" must {

    val view = viewFor[HowManyEntriesView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like stringPage(form, applyView, messageKeyPrefix, routes.HowManyEntriesController.onSubmit(NormalMode).url)
  }
}
