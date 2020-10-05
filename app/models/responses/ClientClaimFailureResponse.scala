package models.responses

import play.api.libs.json.{Json, OFormat}

final case class ClientClaimFailureResponse(
                                             correlationID: String,
                                             processingDate: String,
                                             errorCode: String, //confirm?
                                             errorMessage: String,
                                             pxObjClass: Option[String]
                                           )


object ClientClaimFailureResponse {

  implicit val format: OFormat[ClientClaimFailureResponse] = Json.format[ClientClaimFailureResponse]

}