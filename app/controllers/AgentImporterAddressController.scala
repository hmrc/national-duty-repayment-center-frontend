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

import connectors.AddressLookupConnector
import controllers.actions._
import forms.{AddressSelectionFormProvider, AgentImporterAddressFormProvider, PostcodeFormProvider}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Address, PostcodeLookup}
import navigation.CreateNavigator
import org.slf4j.LoggerFactory
import pages.{AgentImporterAddressPage, AgentImporterManualAddressPage, AgentImporterPostcodePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddressSorter
import views.html.{AgentImporterAddressConfirmationView, AgentImporterAddressView}

import scala.concurrent.{ExecutionContext, Future}

class AgentImporterAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AgentImporterAddressFormProvider,
  postcodeFormProvider: PostcodeFormProvider,
  addressSelectionFormProvider: AddressSelectionFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AgentImporterAddressView,
  addressLookupConnector: AddressLookupConnector,
  sorter: AddressSorter,
  addressConfirmationView: AgentImporterAddressConfirmationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private val postcodeForm  = postcodeFormProvider()
  private val selectionForm = addressSelectionFormProvider()
  val logger                = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(AgentImporterPostcodePage) match {
        case None => postcodeForm
        case Some(value) =>
          postcodeForm.fill(PostcodeLookup(value))
      }

      Future.successful(Ok(view(preparedForm, navigator.previousPage(AgentImporterAddressPage, request.userAnswers))))
  }

  def postcodeSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {

    implicit request =>
      postcodeForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, navigator.previousPage(AgentImporterAddressPage, request.userAnswers)))
          ),
        lookup =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentImporterPostcodePage, lookup.postcode))
            _              <- sessionRepository.set(updatedAnswers)
            lookupResult   <- doPostcodeLookup(lookup, selectionForm)
          } yield lookupResult
      )
  }

  private def doPostcodeLookup(lookup: PostcodeLookup, form: Form[JsObject])(implicit
    hc: HeaderCarrier,
    request: DataRequest[_]
  ): Future[Result] =
    addressLookupConnector.addressLookup(lookup) map {
      case Left(err) =>
        logger.warn(s"Address lookup failure $err")
        BadRequest(
          view(buildLookupFailureError(lookup), navigator.previousPage(AgentImporterAddressPage, request.userAnswers))
        )

      case Right(candidates) if candidates.noOfHits == 0 =>
        BadRequest(
          view(buildLookupFailureError(lookup), navigator.previousPage(AgentImporterAddressPage, request.userAnswers))
        )

      case Right(candidates) =>
        val selectionItems = sorter.sort(candidates.candidateAddresses)
          .map(Address.fromLookupResponse)
          .map(
            a =>
              SelectItem(
                text = a.AddressLine1 + " " +
                  (if (a.AddressLine2 == None) "" else a.AddressLine2.get) + " " +
                  (if (a.City == None) "" else a.City) + " " +
                  (if (a.Region == None) "" else a.Region),
                value = Some(Json.toJson(a).toString())
              )
          )

        if (form.hasErrors)
          BadRequest(
            addressConfirmationView(
              form,
              lookup,
              selectionItems,
              navigator.previousPage(AgentImporterAddressPage, request.userAnswers)
            )
          )
        else
          Ok(
            addressConfirmationView(
              form,
              lookup,
              selectionItems,
              navigator.previousPage(AgentImporterAddressPage, request.userAnswers)
            )
          )
    }

  private def buildLookupFailureError(lookup: PostcodeLookup) =
    postcodeForm.fill(lookup).withError("address-postcode", "postcode.error.noneFound")

  private def extractSearchTerms(formData: Option[Map[String, Seq[String]]]): Option[PostcodeLookup] = for {
    form     <- formData
    postcode <- form.get("address-postcode").flatMap(_.headOption)
  } yield PostcodeLookup(postcode)

  def addressSelectSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      extractSearchTerms(request.body.asFormUrlEncoded).map { searchTerms =>
        selectionForm.bindFromRequest().fold(
          formWithErrors => doPostcodeLookup(searchTerms, formWithErrors),
          js =>
            form.bind(js).fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, navigator.previousPage(AgentImporterAddressPage, request.userAnswers))
                  )
                ),
              address =>
                for {
                  updatedAnswers             <- Future.fromTry(request.userAnswers.set(AgentImporterAddressPage, address))
                  removeManualAddressAnswers <- Future.fromTry(updatedAnswers.remove(AgentImporterManualAddressPage))
                  _                          <- sessionRepository.set(removeManualAddressAnswers)
                } yield Redirect(navigator.nextPage(AgentImporterAddressPage, removeManualAddressAnswers))
            )
        )
      }.getOrElse(Future.successful(Redirect(routes.AgentImporterAddressController.enteredAddressPageLoad())))
  }

  def enteredAddressPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AgentImporterAddressPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, navigator.previousPage(AgentImporterAddressPage, request.userAnswers)))
  }

}
