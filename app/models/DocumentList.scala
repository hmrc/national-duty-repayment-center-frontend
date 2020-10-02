package models

import play.api.libs.json.{Json, OFormat}

final case class DocumentList(
                               `type`: String,
                               description: String
                             )

object DocumentList {

  implicit val format: OFormat[DocumentList] = Json.format[DocumentList]

}
