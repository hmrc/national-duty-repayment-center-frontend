package pages

import pages.behaviours.PageBehaviours

class AmendCaseUploadAnotherFilePageSpec extends PageBehaviours {

  "AmendCaseUploadAnotherFilePage" must {

    beRetrievable[Boolean](AmendCaseUploadAnotherFilePage)

    beSettable[Boolean](AmendCaseUploadAnotherFilePage)

    beRemovable[Boolean](AmendCaseUploadAnotherFilePage)
  }
}
