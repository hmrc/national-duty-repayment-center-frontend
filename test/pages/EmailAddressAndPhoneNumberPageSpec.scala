package pages

import pages.behaviours.PageBehaviours


class EmailAddressAndPhoneNumberPageSpec extends PageBehaviours {

  "EmailAddressPage" must {

    beRetrievable[String](EmailAddressAndPhoneNumberPage)

    beSettable[String](EmailAddressAndPhoneNumberPage)

    beRemovable[String](EmailAddressAndPhoneNumberPage)
  }
}