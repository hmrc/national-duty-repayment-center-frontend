package pages

import pages.behaviours.PageBehaviours


class customsDutyPaidPageSpec extends PageBehaviours {

  "customsDutyPaidPage" must {

    beRetrievable[String](customsDutyPaidPage)

    beSettable[String](customsDutyPaidPage)

    beRemovable[String](customsDutyPaidPage)
  }
}
