package models

import play.api.libs.json.Format

case class AccountName(value: String)

object ApplicationType {
  implicit val format: Format[AccountName] =
    JsonFormatUtils.stringFormat(AccountName.apply)(_.value)
}
