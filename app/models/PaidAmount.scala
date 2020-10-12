package models

import play.api.libs.json.Format

case class PaidAmount(value: String)

object PaidAmount {
  implicit val format: Format[PaidAmount] =
    JsonFormatUtils.stringFormat(PaidAmount.apply)(_.value)
}
