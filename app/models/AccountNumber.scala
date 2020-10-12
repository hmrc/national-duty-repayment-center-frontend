package models

import play.api.libs.json.Format

case class AccountNumber(value: String)

object ApplicationType {
  implicit val format: Format[AccountNumber] =
    JsonFormatUtils.stringFormat(AccountNumber.apply)(_.value)
}
