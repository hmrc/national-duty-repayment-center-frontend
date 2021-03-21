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
import forms.EntryDetailsFormProvider
import javax.inject.Inject
import models.{CustomsRegulationType, Mode, NumberOfEntriesType, UserAnswers}
import navigation.Navigator
import pages.{CustomsRegulationTypePage, EntryDetailsPage, NumberOfEntriesTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.EntryDetailsView

import scala.concurrent.{ExecutionContext, Future}

class EntryDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: EntryDetailsFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: EntryDetailsView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  private def getBackLink(mode: Mode, userAnswers: UserAnswers): Call = {

    userAnswers.get(NumberOfEntriesTypePage).get.numberOfEntriesType match {
      case NumberOfEntriesType.Single if userAnswers.get(CustomsRegulationTypePage).contains(CustomsRegulationType.UKCustomsCodeRegulation) => routes.UkRegulationTypeController.onPageLoad(mode)
      case NumberOfEntriesType.Single if userAnswers.get(CustomsRegulationTypePage).contains(CustomsRegulationType.UnionsCustomsCodeRegulation) => routes.ArticleTypeController.onPageLoad(mode)
      case _ => routes.BulkFileUploadController.showFileUpload(mode)
    }
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(EntryDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getBackLink(mode, request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, getBackLink(mode, request.userAnswers)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EntryDetailsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EntryDetailsPage, mode, updatedAnswers))
      )
  }
}
