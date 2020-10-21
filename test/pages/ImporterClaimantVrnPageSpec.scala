package pages

import pages.behaviours.PageBehaviours


class ImporterClaimantVrnPageSpec extends PageBehaviours {

  "ImporterClaimantVrnPage" must {

    beRetrievable[String](ImporterClaimantVrnPage)

    beSettable[String](ImporterClaimantVrnPage)

    beRemovable[String](ImporterClaimantVrnPage)
  }
}
