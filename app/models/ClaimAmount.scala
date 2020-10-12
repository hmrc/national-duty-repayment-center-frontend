package models

import play.api.libs.json.Format

case class ClaimAmount(value: String)

object ClaimAmount {
  implicit val format: Format[ClaimAmount] =
    JsonFormatUtils.stringFormat(ClaimAmount.apply)(_.value)
}
