/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.ImporterEoriFormProvider
import models.UserAnswers
import navigation.CreateNavigator
import pages.{ImporterEoriOnAgentJourneyPage, ImporterEoriPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ImporterEoriView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImporterEoriController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ImporterEoriFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ImporterEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ImporterEoriPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, backLink(request.userAnswers)))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, backLink(request.userAnswers)))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ImporterEoriPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(nextPage(updatedAnswers))
      )
  }

  private def backLink(answers: UserAnswers) =
    if (answers.isImporterJourney)
      navigator.previousPage(ImporterEoriPage, answers)
    else
      navigator.previousPage(ImporterEoriOnAgentJourneyPage, answers)

  private def nextPage(answers: UserAnswers) =
    if (answers.isImporterJourney)
      navigator.nextPage(ImporterEoriPage, answers)
    else
      navigator.nextPage(ImporterEoriOnAgentJourneyPage, answers)

}
