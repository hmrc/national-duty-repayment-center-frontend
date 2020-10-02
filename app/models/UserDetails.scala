package models

import play.api.libs.json.{Json, OFormat}

final case class UserDetails(
                             vatNumber: String,
                             eori: String,
                             name: String,
                             address: String,
                           )

object UserDetails{

  implicit val format: OFormat[UserDetails] = Json.format[UserDetails]

}