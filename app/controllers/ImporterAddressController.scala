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
import forms.{AddressSelectionFormProvider, ImporterAddressFormProvider, PostcodeFormProvider}
import javax.inject.Inject
import models.{Address, ClaimantType, Mode, PostcodeLookup, UserAnswers}
import navigation.Navigator
import pages.{ClaimantTypePage, ImporterAddressPage, ImporterPostcodePage}
import org.slf4j.LoggerFactory
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Request, Result}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.SelectItem
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddressSorter
import views.html.{ImporterAddressConfirmationView, ImporterAddressView}

import scala.concurrent.{ExecutionContext, Future}

class ImporterAddressController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           sessionRepository: SessionRepository,
                                           navigator: Navigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: ImporterAddressFormProvider,
                                           postcodeFormProvider: PostcodeFormProvider,
                                           addressSelectionFormProvider: AddressSelectionFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: ImporterAddressView,
                                           addressLookupConnector: AddressLookupConnector,
                                           sorter: AddressSorter,
                                           addressConfirmationView : ImporterAddressConfirmationView
                                         )(implicit ec: ExecutionContext)
                                          extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val postcodeForm = postcodeFormProvider()
  private val selectionForm = addressSelectionFormProvider()
  val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  private def getBackLink(mode: Mode): Call = {
    routes.ImporterNameController.onPageLoad(mode)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(ImporterPostcodePage) match {
        case None => postcodeForm
        case Some(value) =>
          postcodeForm.fill(PostcodeLookup(value))
      }

      Future.successful(Ok(view(preparedForm, mode, isImporterJourney(request.userAnswers), getBackLink(mode))))
  }

  def postcodeSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {

    implicit request =>
      postcodeForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, isImporterJourney(request.userAnswers), getBackLink(mode)))),
        lookup => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ImporterPostcodePage, lookup.postcode))
            _              <- sessionRepository.set(updatedAnswers)
            lookupResult   <- doPostcodeLookup(lookup, mode, selectionForm, isImporterJourney(request.userAnswers))
          } yield lookupResult
        }
      )
  }

  private def doPostcodeLookup(lookup: PostcodeLookup, mode: Mode, form: Form[JsObject], isImporterJourney: Boolean)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {

    addressLookupConnector.addressLookup(lookup) map {
      case Left(err) =>
        logger.warn(s"Address lookup failure $err")
        BadRequest(view(buildLookupFailureError(lookup), mode, isImporterJourney, getBackLink(mode)))

      case Right(candidates) if candidates.noOfHits == 0 =>
        BadRequest(view(buildLookupFailureError(lookup), mode, isImporterJourney, getBackLink(mode)))

      case Right(candidates) =>
        val selectionItems = sorter.sort(candidates.candidateAddresses)
          .map(Address.fromLookupResponse)
          .map(a =>
            SelectItem(
            text = a.AddressLine1+" "+
              (if (a.AddressLine2 == None) "" else a.AddressLine2.get)+" "+
              (if (a.City == None) "" else a.City)+" "+
              (if (a.Region == None) "" else a.Region),
            value = Some(Json.toJson(a).toString()))
          )

        if (form.hasErrors) {
          BadRequest(addressConfirmationView(form, lookup, selectionItems, mode, isImporterJourney))
        } else {
          Ok(addressConfirmationView(form, lookup, selectionItems, mode, isImporterJourney))
        }
    }
  }

  private def buildLookupFailureError(lookup: PostcodeLookup) =
      postcodeForm.fill(lookup).withError("address-postcode", "postcode.error.noneFound")

  private def extractSearchTerms(formData: Option[Map[String, Seq[String]]]): Option[PostcodeLookup] = for {
    form           <- formData
    postcode       <- form.get("address-postcode").flatMap(_.headOption)
  } yield PostcodeLookup(postcode)

  def addressSelectSubmit(mode: Mode): Action[AnyContent] = (identify  andThen getData andThen requireData).async {
    implicit request =>

      extractSearchTerms(request.body.asFormUrlEncoded).map { searchTerms =>
        selectionForm.bindFromRequest().fold(
          formWithErrors =>
            doPostcodeLookup(searchTerms, mode, formWithErrors, isImporterJourney(request.userAnswers)),

          js =>
            form.bind(js).fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, isImporterJourney(request.userAnswers), getBackLink(mode)))),
              address =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ImporterAddressPage, address))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield {
                  request.userAnswers.get(ClaimantTypePage) match {
                    case Some(ClaimantType.Importer) => Redirect(routes.PhoneNumberController.onPageLoad(mode))
                    case _ => Redirect(routes.ImporterHasEoriController.onPageLoad(mode))
                  }
                }
            )
        )
      }.getOrElse(Future.successful(Redirect(routes.ImporterAddressController.enteredAddressPageLoad())))
  }

  def enteredAddressPageLoad(mode: Mode): Action[AnyContent] = (identify  andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ImporterAddressPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, isImporterJourney(request.userAnswers), getBackLink(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, isImporterJourney(request.userAnswers), getBackLink(mode)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ImporterAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ImporterAddressPage, mode, updatedAnswers))
      )
  }

  def isImporterJourney(userAnswers: UserAnswers): Boolean = {
    userAnswers.get(ClaimantTypePage) match {
      case Some(ClaimantType.Importer) => true
      case _ => false
    }
  }

}
