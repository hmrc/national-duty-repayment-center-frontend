package pages

import models.BulkFileUpload
import pages.behaviours.PageBehaviours

class BulkFileUploadPageSpec extends PageBehaviours {

  "BulkFileUploadPage" must {

    beRetrievable[BulkFileUpload](BulkFileUploadPage)

    beSettable[BulkFileUpload](BulkFileUploadPage)

    beRemovable[BulkFileUpload](BulkFileUploadPage)
  }
}
