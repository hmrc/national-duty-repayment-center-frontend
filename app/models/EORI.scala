package models

import play.api.libs.json.Format

case class EORI(value: String)

object ApplicationType {
  implicit val format: Format[EORI] =
    JsonFormatUtils.stringFormat(EORI.apply)(_.value)
}
