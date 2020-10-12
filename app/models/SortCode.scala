package models

import play.api.libs.json.Format

case class SortCode(value: String)

object ApplicationType {
  implicit val format: Format[SortCode] =
    JsonFormatUtils.stringFormat(SortCode.apply)(_.value)

  val validationRegex: String = ???
}
