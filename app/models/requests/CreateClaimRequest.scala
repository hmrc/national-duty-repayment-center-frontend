package models.requests

import models.Content

class CreateClaimRequest {

  import play.api.libs.json.{Json, OFormat}

  final case class CreateClaimRequest(
                                       acknowledgementReference: String,
                                       applicationType: String,
                                       originatingSystem: String,
                                       content: Content
                                     )

  object CreateClaimRequest {
    implicit val formats: OFormat[CreateClaimRequest] = Json.format[CreateClaimRequest]

  }

}
