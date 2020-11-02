package views

import controllers.routes
import forms.ImporterAddressFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.ImporterAddressView

class ImporterAddressViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "importerAddress"

  val form = new ImporterAddressFormProvider()()

  "ImporterAddressView view" must {

    val view = viewFor[ImporterAddressView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like stringPage(form, applyView, messageKeyPrefix, routes.ImporterAddressController.onSubmit(NormalMode).url)
  }
}
