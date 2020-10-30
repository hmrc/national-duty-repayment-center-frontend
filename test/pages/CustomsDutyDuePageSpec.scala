package pages

import pages.behaviours.PageBehaviours


class CustomsDutyDuePageSpec extends PageBehaviours {

  "CustomsDutyDuePage" must {

    beRetrievable[String](CustomsDutyDuePage)

    beSettable[String](CustomsDutyDuePage)

    beRemovable[String](CustomsDutyDuePage)
  }
}
