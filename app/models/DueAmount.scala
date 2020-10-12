package models

import play.api.libs.json.Format

case class DueAmount(value: String)

object DueAmount {
  implicit val format: Format[DueAmount] =
    JsonFormatUtils.stringFormat(DueAmount.apply)(_.value)
}
