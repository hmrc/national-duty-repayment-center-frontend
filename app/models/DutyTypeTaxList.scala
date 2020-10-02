package models

import play.api.libs.json.{Json, OFormat}

final case class DutyTypeTaxList(
                                  `type`: String,
                                  paidAmount: String,
                                  dueAmount: String,
                                  ClaimAmount: String
                                )

object DutyTypeTaxList{
  implicit val format: OFormat[DutyTypeTaxList] = Json.format[DutyTypeTaxList]

}