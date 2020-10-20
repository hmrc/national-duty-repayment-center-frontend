package pages

import pages.behaviours.PageBehaviours

class ImporterHasEoriPageSpec extends PageBehaviours {

  "ImporterHasEoriPage" must {

    beRetrievable[Boolean](ImporterHasEoriPage)

    beSettable[Boolean](ImporterHasEoriPage)

    beRemovable[Boolean](ImporterHasEoriPage)
  }
}
