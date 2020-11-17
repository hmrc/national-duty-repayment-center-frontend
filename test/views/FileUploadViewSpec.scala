package views

import views.behaviours.ViewBehaviours
import views.html.FileUploadView

class FileUploadViewSpec extends ViewBehaviours {

  "FileUpload view" must {

    val view = viewFor[FileUploadView](Some(emptyUserAnswers))

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView, "fileUpload")

    behave like pageWithBackLink(applyView)
  }
}
