/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{Address, Country, UserAnswers}
import navigation.CreateNavigator
import pages.{AgentImporterAddressPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressLookupService, CountryService}
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
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
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentImporterAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(nextPage(updatedAnswers))
      )
  }

  def onChange(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      addressLookupService.initialiseJourney(
        controllers.routes.AgentImporterAddressFrontendController.onUpdate("").url,
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

  def onUpdate(id: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      addressLookupService.retrieveAddress(id) flatMap { confirmedAddress =>
        val el = confirmedAddress.extractAddressLines()
        val updatedAddress = Address(
          el._1,
          el._2,
          el._4,
          None,
          Country(confirmedAddress.address.country.code, confirmedAddress.address.country.name),
          confirmedAddress.address.postcode.getOrElse("")
        )
        // Address Lookup Service may return an address that is incompatible with NDRC, so validate it again
        val formWithAddress = form.fillAndValidate(updatedAddress)
        if (formWithAddress.hasErrors)
          Future.successful(BadRequest(addressView(formWithAddress, backLink(request.userAnswers), countrySelectItems)))
        else
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentImporterAddressPage, updatedAddress))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(nextPage(updatedAnswers))
      }
  }

}
