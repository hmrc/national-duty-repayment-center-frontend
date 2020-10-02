package models

import models.requests.ClaimDetails
import play.api.libs.json.{Json, OFormat}

final case class Content(
                          claimDetails: ClaimDetails,
                          agentDetails: AgentDetails,
                          importerDetails: ImporterDetails,
                          bankDetails: AllBankDetails,
                          dutyTypeTaxList: DutyTypeTaxList,
                          documentList: DocumentList
                        )

object Content {

  implicit val formats: OFormat[Content] = Json.format[Content]

}