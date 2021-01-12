package views

import controllers.routes
import forms.AmendCaseUploadAnotherFileFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.AmendCaseUploadAnotherFileView

class AmendCaseUploadAnotherFileViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "amendCaseUploadAnotherFile"

  val form = new AmendCaseUploadAnotherFileFormProvider()()

  "AmendCaseUploadAnotherFile view" must {

    val view = viewFor[AmendCaseUploadAnotherFileView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, routes.AmendCaseUploadAnotherFileController.onSubmit(NormalMode).url)
  }
}
