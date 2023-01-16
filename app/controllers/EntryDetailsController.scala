/*
 * Copyright 2023 HM Revenue & Customs
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
import models.CustomsRegulationType.UnionsCustomsCodeRegulation
import models.UserAnswers
import navigation.CreateNavigator
import pages.{EntryDetailsPage, _}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EntryDetailsView

import scala.concurrent.{ExecutionContext, Future}

class EntryDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EntryDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EntryDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = EntryDetailsPage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(EntryDetailsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(
        view(
          preparedForm,
          backLink(request.userAnswers),
          request.userAnswers.isImporterJourney,
          request.userAnswers.isSingleEntry
        )
      )

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              view(
                formWithErrors,
                backLink(request.userAnswers),
                request.userAnswers.isImporterJourney,
                request.userAnswers.isSingleEntry
              )
            )
          ),
        value =>
          for {
            // save entry details
            userAnswers <- Future.fromTry(request.userAnswers.set(EntryDetailsPage, value))
            // clear redundant regulation/article answer
            userAnswers <- userAnswers.customsRegulationType match {
              case Some(UnionsCustomsCodeRegulation) =>
                Future.fromTry(userAnswers.remove(UkRegulationTypePage))
              case _ => Future.fromTry(userAnswers.remove(ArticleTypePage))
            }
            _ <- sessionRepository.set(userAnswers)
          } yield Redirect(nextPage(userAnswers))
      )
  }

}
