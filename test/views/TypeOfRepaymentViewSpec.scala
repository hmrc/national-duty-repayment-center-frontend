package views

import forms.TypeOfRepaymentFormProvider
import models.{TypeOfRepayment, NormalMode}
import play.api.Application
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.CheckboxViewBehaviours
import views.html.TypeOfRepaymentView

class TypeOfRepaymentViewSpec extends CheckboxViewBehaviours[TypeOfRepayment] {

  val messageKeyPrefix = "typeOfRepayment"

  val form = new TypeOfRepaymentFormProvider()()

  "TypeOfRepaymentView" must {

    val view = viewFor[TypeOfRepaymentView](Some(emptyUserAnswers))

    def applyView(form: Form[Set[TypeOfRepayment]]): HtmlFormat.Appendable =
      view.apply(form, NormalMode)(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like checkboxPage(form, applyView, messageKeyPrefix, TypeOfRepayment.options)
  }
}
