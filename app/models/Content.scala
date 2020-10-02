package models

import models.requests.ClaimDetails
import play.api.libs.json.{Json, OFormat}

final case class Content(
                          claimDetails: ClaimDetails,
                          agentDetails: UserDetails,
                          importerDetails: UserDetails,
                          bankDetails: AllBankDetails,
                          dutyTypeTaxList: Seq[DutyTypeTaxList],
                          documentList: Seq[DocumentList]
                        )

object Content {

  implicit val formats: OFormat[Content] = Json.format[Content]

}