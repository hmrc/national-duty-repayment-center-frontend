package pages

import pages.behaviours.PageBehaviours


class ImporterAddressPageSpec extends PageBehaviours {

  "ImporterAddressPage" must {

    beRetrievable[String](ImporterAddressPage)

    beSettable[String](ImporterAddressPage)

    beRemovable[String](ImporterAddressPage)
  }
}
