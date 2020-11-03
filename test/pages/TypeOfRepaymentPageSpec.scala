package pages

import models.TypeOfRepayment
import pages.behaviours.PageBehaviours

class TypeOfRepaymentPageSpec extends PageBehaviours {

  "TypeOfRepaymentPage" must {

    beRetrievable[Set[TypeOfRepayment]](TypeOfRepaymentPage)

    beSettable[Set[TypeOfRepayment]](TypeOfRepaymentPage)

    beRemovable[Set[TypeOfRepayment]](TypeOfRepaymentPage)
  }
}
