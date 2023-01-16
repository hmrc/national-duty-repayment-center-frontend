/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.actions._
import forms.AgentImporterManualAddressFormProvider
import javax.inject.Inject
import models.addresslookup.MissingAddressIdException
import models.requests.DataRequest
import models.{Address, UserAnswers}
import navigation.CreateNavigator
import pages.{AgentImporterAddressPage, Page}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressLookupService, CountryService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AgentImporterManualAddressView

import scala.concurrent.{ExecutionContext, Future}

class AgentImporterAddressFrontendController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  addressLookupService: AddressLookupService,
  countriesService: CountryService,
  formProvider: AgentImporterManualAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  addressView: AgentImporterManualAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = AgentImporterAddressPage
  val form                = formProvider()

  private val countrySelectItems = countriesService.selectItems()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(AgentImporterAddressPage) match {
        case Some(address) =>
          val preparedForm = form.fill(address)
          Ok(addressView(preparedForm, backLink(request.userAnswers), countrySelectItems))
        case _ =>
          Redirect(controllers.routes.AgentImporterAddressFrontendController.onChange())
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(addressView(formWithErrors, backLink(request.userAnswers), countrySelectItems))),
        _ => {
          val address = formProvider.dataExtractor.bindFromRequest().value.getOrElse(throw new Exception)
          saveAndContinue(address, true)
        }
      )
  }

  def onChange(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      addressLookupService.initialiseJourney(
        // Note: Use 'None' to generate update url without id parameter
        controllers.routes.AgentImporterAddressFrontendController.onUpdate(None).url,
        controllers.routes.IndexController.onPageLoad().url,
        controllers.routes.SignOutController.signOut().url,
        controllers.routes.KeepAliveController.keepAlive().url,
        "agentImporterAddress.title",
        "agentImporterManualAddress.hint",
        "agentImporterManualAddress.title",
        "agentImporterManualAddress.confirmation.title"
      ) map {
        response => Redirect(response.redirectUrl)
      }
  }

  def onUpdate(id: Option[String]): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      addressLookupService.retrieveAddress(id.getOrElse(throw new MissingAddressIdException)) flatMap {
        confirmedAddress =>
          val el = confirmedAddress.extractAddressLines()
          val updatedAddress = Address(
            el._1,
            el._2,
            el._4,
            None,
            // ensure country returned from ALF exists in our service
            countriesService.find(confirmedAddress.address.country.code),
            confirmedAddress.address.postcode,
            Some(confirmedAddress.auditRef)
          )
          // Address Lookup Service may return an address that is incompatible with NDRC, so validate it again
          // Note: simple `form.fillAndValidate(updatedAddress)` doesn't work with conditional PostCode validation
          val data = form.mapping.unbind(updatedAddress)
          form.mapping.bind(data) match {
            case Left(errors) =>
              Future.successful(
                BadRequest(
                  addressView(
                    Form[Address](form.mapping, data, errors, None),
                    backLink(request.userAnswers),
                    countrySelectItems
                  )
                )
              )
            case Right(_) => saveAndContinue(updatedAddress, false)
          }
      }
  }

  private def saveAndContinue(address: Address, checkAuditRef: Boolean)(implicit request: DataRequest[_]) = {
    val updateAuditRef =
      if (checkAuditRef && !request.userAnswers.get(AgentImporterAddressPage).contains(address))
        address.copy(auditRef = None)
      else
        address
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentImporterAddressPage, updateAuditRef))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(nextPage(updatedAnswers))
  }

}
