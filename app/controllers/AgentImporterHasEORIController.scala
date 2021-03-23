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
import forms.AgentImporterHasEORIFormProvider

import javax.inject.Inject
import models.{AgentImporterHasEORI, CheckMode, Mode}
import navigation.Navigator
import pages.{AgentImporterHasEORIPage, EnterAgentEORIPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.AgentImporterHasEORIView

import scala.concurrent.{ExecutionContext, Future}

class AgentImporterHasEORIController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: AgentImporterHasEORIFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AgentImporterHasEORIView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  private def getBackLink(mode: Mode): Call = {
    routes.FileUploadController.showFileUploaded(mode)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AgentImporterHasEORIPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getBackLink(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val oldEORI = if(request.userAnswers.get(AgentImporterHasEORIPage).nonEmpty)
        request.userAnswers.get(AgentImporterHasEORIPage).get

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, getBackLink(mode)))),

        value =>
          for {
            setEORINumber <- Future.fromTry (request.userAnswers.set(AgentImporterHasEORIPage, value))
            updatedAnswers <- {
              Future.fromTry(setEORINumber.get(AgentImporterHasEORIPage).get match {
                case AgentImporterHasEORI.No => setEORINumber.remove(EnterAgentEORIPage)
                case AgentImporterHasEORI.Yes => setEORINumber.set(AgentImporterHasEORIPage, value)
              })
            }
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            if (mode.equals(CheckMode)) {
              (oldEORI, value) match {
                case (AgentImporterHasEORI.No, AgentImporterHasEORI.Yes) => Redirect(navigator.nextPage(AgentImporterHasEORIPage, CheckMode, updatedAnswers))
                case _ => Redirect(routes.CheckYourAnswersController.onPageLoad())
              }
            } else
              Redirect(navigator.nextPage(AgentImporterHasEORIPage, mode, updatedAnswers))
          }
      )
  }
}
