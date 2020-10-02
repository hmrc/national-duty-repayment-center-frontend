package models

import play.api.libs.json.{Json, OFormat}

final case class AgentDetails(
                               vatNumber:String,
                              eori:String,
                              name:String,
                              address:String
                             )

  object AgentDetails{

    implicit val format: OFormat[AgentDetails] = Json.format[AgentDetails]

  }
