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
import forms.ImporterHasEoriFormProvider
import javax.inject.Inject
import models.UserAnswers
import navigation.CreateNavigator
import pages.{ImportHasEoriOnAgentJourneyPage, ImporterEoriPage, ImporterHasEoriPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ImporterHasEoriView

import scala.concurrent.{ExecutionContext, Future}

class ImporterHasEoriController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ImporterHasEoriFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ImporterHasEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ImporterHasEoriPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, backLink(request.userAnswers), request.userAnswers.isImporterJourney))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, backLink(request.userAnswers), request.userAnswers.isImporterJourney))
          ),
        value =>
          for {
            setEORINumber <- Future.fromTry(request.userAnswers.set(ImporterHasEoriPage, value))
            updatedAnswers <-
              Future.fromTry(setEORINumber.get(ImporterHasEoriPage).get match {
                case false => setEORINumber.remove(ImporterEoriPage)
                case true  => setEORINumber.set(ImporterHasEoriPage, value)
              })
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(nextPage(updatedAnswers))
      )
  }

  private def backLink(answers: UserAnswers) =
    if (answers.isImporterJourney)
      navigator.previousPage(ImporterHasEoriPage, answers)
    else
      navigator.previousPage(ImportHasEoriOnAgentJourneyPage, answers)

  private def nextPage(answers: UserAnswers) =
    if (answers.isImporterJourney)
      navigator.nextPage(ImporterHasEoriPage, answers)
    else
      navigator.nextPage(ImportHasEoriOnAgentJourneyPage, answers)

}
