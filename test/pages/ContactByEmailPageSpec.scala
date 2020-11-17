package pages

import models.ContactByEmail
import pages.behaviours.PageBehaviours

class ContactByEmailSpec extends PageBehaviours {

  "ContactByEmailPage" must {

    beRetrievable[ContactByEmail](ContactByEmailPage)

    beSettable[ContactByEmail](ContactByEmailPage)

    beRemovable[ContactByEmail](ContactByEmailPage)
  }
}
