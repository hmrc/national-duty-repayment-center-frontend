package views

import controllers.routes
import forms.ImporterClaimantVrnFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.ImporterClaimantVrnView

class ImporterClaimantVrnViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "importerClaimantVrn"

  val form = new ImporterClaimantVrnFormProvider()()

  "ImporterClaimantVrnView view" must {

    val view = viewFor[ImporterClaimantVrnView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like stringPage(form, applyView, messageKeyPrefix, routes.ImporterClaimantVrnController.onSubmit(NormalMode).url)
  }
}
