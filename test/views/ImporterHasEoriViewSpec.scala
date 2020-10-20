package views

import controllers.routes
import forms.ImporterHasEoriFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.ImporterHasEoriView

class ImporterHasEoriViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "importerHasEori"

  val form = new ImporterHasEoriFormProvider()()

  "ImporterHasEori view" must {

    val view = viewFor[ImporterHasEoriView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, routes.ImporterHasEoriController.onSubmit(NormalMode).url)
  }
}
