/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.{AddressFormProvider, AddressSelectionFormProvider, ImporterAddressFormProvider, PostcodeFormProvider}
import javax.inject.Inject
import models.requests.IdentifierRequest
import models.{Address, Mode, PostcodeLookup}
import navigation.Navigator
import pages.{ImporterAddressPage, ImporterPostcodePage}
import org.slf4j.LoggerFactory
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddressSorter
import views.html.{ImporterAddressConfirmationView, ImporterAddressView}

import scala.collection.script.Index
import scala.concurrent.{ExecutionContext, Future}

class ImporterAddressController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           sessionRepository: SessionRepository,
                                           navigator: Navigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           addressFormProvider: ImporterAddressFormProvider,
                                           postcodeFormProvider: PostcodeFormProvider,
                                           addressSelectionFormProvider: AddressSelectionFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: ImporterAddressView,
                                           addressLookupConnector: AddressLookupConnector,
                                           sorter: AddressSorter,
                                           addressConfirmationView : ImporterAddressConfirmationView
                                         )(implicit ec: ExecutionContext)
                                          extends FrontendBaseController with I18nSupport {

  private val form = addressFormProvider()
  private val postcodeForm = postcodeFormProvider()
  private val selectionForm = addressSelectionFormProvider()
  val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ImporterAddressPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def postcodeSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      postcodeForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        lookup => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ImporterPostcodePage, lookup.postcode))
            _              <- sessionRepository.set(updatedAnswers)
            lookupResult   <- doPostcodeLookup(lookup, mode, selectionForm)
          } yield lookupResult
        }
      )
  }

  private def doPostcodeLookup(lookup: PostcodeLookup, mode: Mode, form: Form[JsObject]): Future[Result] = {
    addressLookupConnector.addressLookup(lookup) map {
      case Left(err) =>
        logger.warn(s"Address lookup failure $err")
        BadRequest(view(buildLookupFailureError(lookup), mode))

      case Right(candidates) if candidates.noOfHits == 0 =>
        BadRequest(view(buildLookupFailureError(lookup), mode))

      case Right(candidates) =>
        val selectionItems = sorter.sort(candidates.candidateAddresses)
          .map(Address.fromLookupResponse)
          .map(a => RadioItem(
            content = Text(a.inlineText),
            value = Some(Json.toJson(a).toString())))
        if (form.hasErrors) {
          BadRequest(addressConfirmationView(lookup, selectionItems))
        } else {
          Ok(addressConfirmationView(lookup, selectionItems))
        }
    }
  }

  private def buildLookupFailureError(lookup: PostcodeLookup) =
    if (lookup.houseNumber.isDefined) {
      postcodeForm.fill(lookup).withError("address-propertyNumber", "postcode.propertyNumber.error.noneFound")
    } else {
      postcodeForm.fill(lookup).withError("address-postcode", "postcode.error.noneFound")
    }


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ImporterAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ImporterAddressPage, mode, updatedAnswers))
      )
  }

}
