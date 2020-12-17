package pages

import models.BulkFileUpload
import pages.behaviours.PageBehaviours

class BulkFileUploadPageSpec extends PageBehaviours {

  "BulkFileUploadPage" must {

    beRetrievable[Set[BulkFileUpload]](BulkFileUploadPage)

    beSettable[Set[BulkFileUpload]](BulkFileUploadPage)

    beRemovable[Set[BulkFileUpload]](BulkFileUploadPage)
  }
}
