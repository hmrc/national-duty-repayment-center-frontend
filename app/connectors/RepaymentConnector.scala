package connectors

import com.google.inject.Inject
import config.Service
import models.requests.CreateClaimRequest
import models.responses.ClientClaimSuccessResponse
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class RepaymentConnector @Inject()(
                                    config: Configuration,
                                    httpClient: HttpClient
                                  )(
                                    implicit ec: ExecutionContext
                                  ) extends HttpErrorFunctions {

  private val baseUrl = config.get[Service]("microservice.services.national-duty-repayment-center-frontend")


  def submitRepayment(request: CreateClaimRequest)(implicit hc: HeaderCarrier): Future[ClientClaimSuccessResponse] = {
    val url = s"$baseUrl/discounted-dining-participant/submit-registration"

    httpClient.POST[CreateClaimRequest, ClientClaimSuccessResponse](url, request)
  }


  def getRepayment(request: CreateClaimRequest)(implicit hc: HeaderCarrier): Future[Option[ClientClaimSuccessResponse]] = {
    val url = s"$baseUrl/discounted-dining-participant/get-registration"

    httpClient.POST[CreateClaimRequest, ClientClaimSuccessResponse](url, request).map(Some(_))
  }.recover {
    case _: NotFoundException => None
  }
}
