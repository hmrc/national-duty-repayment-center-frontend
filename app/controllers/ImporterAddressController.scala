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

import controllers.actions._
import forms.ImporterAddressFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.ImporterAddressPage
import org.slf4j.LoggerFactory
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ImporterAddressView

import scala.collection.script.Index
import scala.concurrent.{ExecutionContext, Future}

class ImporterAddressController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           sessionRepository: SessionRepository,
                                           navigator: Navigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: ImporterAddressFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: ImporterAddressView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()


  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  private val addressForm = addressFormProvider()
  private val postcodeForm = postcodeFormProvider()
  private val selectionForm = addressSelectionFormProvider()

  def postcodePageLoad(index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkRegistration andThen getData andThen requireData).async { implicit request =>
      requirePreviousAnswers(index, mode, request.userAnswers) {
        val preparedForm = request.userAnswers.get(RestaurantPostcodePage(index)) match {
          case None => postcodeForm
          case Some(value) =>
            postcodeForm.fill(PostcodeLookup(value, None))
        }
        Future.successful(Ok(postcodeView(preparedForm, index, mode)))
      }
    }

  def postcodeSubmit(index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkRegistration andThen getData andThen requireData).async { implicit request =>
      requirePreviousAnswers(index, mode, request.userAnswers) {
        postcodeForm.bindFromRequest.fold(
          formWithErrors =>
            Future.successful(BadRequest(postcodeView(formWithErrors, index, mode))),
          lookup => {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RestaurantPostcodePage(index), lookup.postcode))
              _ <- sessionRepository.set(updatedAnswers)
              lookupResult <- doPostcodeLookup(lookup, index, mode, selectionForm)
            } yield lookupResult
          }
        )
      }
    }

  private def doPostcodeLookup(lookup: PostcodeLookup,
                               index: Index,
                               mode: Mode,
                               form: Form[JsObject])(implicit request: RequestWithInternalId[_]): Future[Result] = {
    addressLookupConnector.addressLookup(lookup) map {
      case Left(err) =>
        logger.warn(s"Address lookup failure $err")
        BadRequest(postcodeView(buildLookupFailureError(lookup), index, mode))

      case Right(candidates) if candidates.noOfHits == 0 =>
        BadRequest(postcodeView(buildLookupFailureError(lookup), index, mode))

      case Right(candidates) =>
        val selectionItems = sorter.sort(candidates.candidateAddresses)
          .map(RestaurantLocationInfo.fromLookupResponse)
          .map(a => RadioItem(
            content = Text(a.address.inlineText),
            value = Some(Json.toJson(a).toString())))
        if (form.hasErrors) {
          BadRequest(addressFoundView(form, lookup, selectionItems, index, mode))
        } else {
          Ok(addressFoundView(form, lookup, selectionItems, index, mode))
        }
    }
  }

  private def buildLookupFailureError(lookup: PostcodeLookup) =
    if (lookup.houseNumber.isDefined) {
      postcodeForm.fill(lookup).withError("address-propertyNumber", "postcode.propertyNumber.error.noneFound")
    } else {
      postcodeForm.fill(lookup).withError("address-postcode", "postcode.error.noneFound")
    }

  private def extractSearchTerms(formData: Option[Map[String, Seq[String]]]): Option[PostcodeLookup] = for {
    form <- formData
    postcode <- form.get("address-postcode").flatMap(_.headOption)
    propertyNumber = form.get("address-propertyNumber").flatMap(_.headOption)
  } yield PostcodeLookup(postcode, propertyNumber)

  def addressSelectOnLoad(index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkRegistration andThen getData andThen requireData).async { request =>
      requirePreviousAnswers(index, mode, request.userAnswers) {
        Future.successful(Redirect(routes.RestaurantAddressController.postcodePageLoad(index, mode)))
      }
    }

  def addressSelectSubmit(index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkRegistration andThen getData andThen requireData).async { implicit request =>
      requirePreviousAnswers(index, mode, request.userAnswers) {
        extractSearchTerms(request.body.asFormUrlEncoded).map { searchTerms =>
          selectionForm.bindFromRequest().fold(
            formWithErrors =>
              doPostcodeLookup(searchTerms, index, mode, formWithErrors),

            js =>
              addressForm.bind(js.value.getOrElse("address", Json.obj())).fold(
                addressFormWithErrors =>
                  Future.successful(BadRequest(addressView(addressFormWithErrors, index, mode))),
                address => {
                  val selectedAddress = RestaurantLocationInfo.format.reads(js).getOrElse(RestaurantLocationInfo(address, None, None))
                  for {
                    updatedAddress <- Future.fromTry(request.userAnswers.set(RestaurantAddressPage(index), selectedAddress.address))
                    updatedUprn <- selectedAddress.uprn match {
                      case Some(uprn) => Future.fromTry(updatedAddress.set(UprnQuery(index), uprn))
                      case None => Future.successful(updatedAddress)
                    }
                    updatedAnswers <- selectedAddress.location match {
                      case Some(location) => Future.fromTry(updatedUprn.set(LocationQuery(index), location))
                      case None => Future.successful(updatedUprn)
                    }
                    _ <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(RestaurantAddressPage(index), mode, updatedAnswers))
                }
              )
          )
        }.getOrElse(Future.successful(Redirect(routes.RestaurantAddressController.enteredAddressPageLoad(index, mode))))
      }
    }

  def enteredAddressPageLoad(index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkRegistration andThen getData andThen requireData).async { implicit request =>
      requirePreviousAnswers(index, mode, request.userAnswers) {

        val preparedForm = request.userAnswers.get(RestaurantAddressPage(index)) match {
          case None => addressForm
          case Some(value) => addressForm.fill(value)
        }

        Future.successful(Ok(addressView(preparedForm, index, mode)))
      }
    }

  def enteredAddressSubmit(index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen checkRegistration andThen getData andThen requireData).async { implicit request =>
      requirePreviousAnswers(index, mode, request.userAnswers) {
        addressForm.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(addressView(formWithErrors, index, mode))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RestaurantAddressPage(index), value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RestaurantAddressPage(index), mode, updatedAnswers))
        )
      }
    }

  private def requirePreviousAnswers(index: Index, mode: Mode, userAnswers: UserAnswers)(block: => Future[Result]): Future[Result] = {
    userAnswers.get(RestaurantNamePage(index)) match {
      case Some(_) => block
      case None => Future.successful(Redirect(routes.RestaurantNameController.onPageLoad(index, mode)))
    }
  }


}
