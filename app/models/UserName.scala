package models

import play.api.libs.json.Format

case class UserName(value: String)

object ApplicationType {
  implicit val format: Format[UserName] =
    JsonFormatUtils.stringFormat(UserName.apply)(_.value)
}
