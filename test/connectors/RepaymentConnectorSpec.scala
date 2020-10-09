package connectors

/*
 * Copyright 2020 HM Revenue & Customs
 *
 */


import java.time.LocalDateTime

import models.responses.ClientClaimSuccessResponse
import org.scalatest.{EitherValues, FreeSpec, MustMatchers, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier


class RepaymentConnectorSpec extends FreeSpec

  with MustMatchers
  with ScalaFutures
  with EitherValues
  with OptionValues
  with IntegrationPatience {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.national-duty-repayment-center-frontend.port" -> server.port
      )
      .build()

  "getRepayment" - {

    "must return a repayment when the server responds with OK" in {

      //val date = LocalDateTime.of(202, 1, 2, 3, 4, 5)
      val response = ClientClaimSuccessResponse("Success", "123", "22/04/2020", Some("200"), Some("PEGA"))

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[RepaymentConnector]

        server.stubFor(
          get(urlEqualTo("national-duty-repayment-center-frontend."))
            .willReturn(
              ok(Json.toJson(response).toString)
            )
        )

        val result = connector.submitRepayment().futureValue

        result.value mustEqual response
      }


    }
