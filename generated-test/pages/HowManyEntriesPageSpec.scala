package pages

import pages.behaviours.PageBehaviours


class HowManyEntriesPageSpec extends PageBehaviours {

  "HowManyEntriesPage" must {

    beRetrievable[String](HowManyEntriesPage)

    beSettable[String](HowManyEntriesPage)

    beRemovable[String](HowManyEntriesPage)
  }
}
