package models.responses

import play.api.libs.json.{Json, OFormat}

final case class ClientClaimSuccessResponse(
                                             status: String,
                                             caseID: String,
                                             processingDate: String,
                                             statusText: Option[String],
                                             pxObjClass: Option[String]
                                           )


object ClientClaimSuccessResponse {

  implicit val format: OFormat[ClientClaimSuccessResponse] = Json.format[ClientClaimSuccessResponse]

}