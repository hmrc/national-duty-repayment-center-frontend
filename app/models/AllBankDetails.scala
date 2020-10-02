package models

import play.api.libs.json.{Json, OFormat}

case class AllBankDetails(agentBankDetails: BankDetails, importerBankDetails: BankDetails)

object AllBankDetails {

  implicit val format: OFormat[AllBankDetails] = Json.format[AllBankDetails]

}
