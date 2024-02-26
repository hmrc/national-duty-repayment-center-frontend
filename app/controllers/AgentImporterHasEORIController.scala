/*
 * Copyright 2024 HM Revenue & Customs
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
import models.{AgentImporterHasEORI, UserAnswers}
import navigation.CreateNavigator
import pages.{AgentImporterHasEORIPage, EnterAgentEORIPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AgentImporterHasEORIView

import scala.concurrent.{ExecutionContext, Future}

class AgentImporterHasEORIController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AgentImporterHasEORIFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AgentImporterHasEORIView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = AgentImporterHasEORIPage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AgentImporterHasEORIPage) match {
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
            setEORINumber <- Future.fromTry(request.userAnswers.set(AgentImporterHasEORIPage, value))
            updatedAnswers <-
              Future.fromTry(setEORINumber.get(AgentImporterHasEORIPage).get match {
                case AgentImporterHasEORI.No  => setEORINumber.remove(EnterAgentEORIPage)
                case AgentImporterHasEORI.Yes => setEORINumber.set(AgentImporterHasEORIPage, value)
              })
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(nextPage(updatedAnswers))
      )
  }

}
