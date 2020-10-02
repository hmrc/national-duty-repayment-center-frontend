package models

import play.api.libs.json.{Json, OFormat}

final case class BankDetails(
                              accountName: String,
                              sortCode: String,
                              accountNumber: String)

object BankDetails {

  implicit val format: OFormat[BankDetails] = Json.format[BankDetails]
}