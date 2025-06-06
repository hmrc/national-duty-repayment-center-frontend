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
import forms.CreateOrAmendCaseFormProvider
import models.CreateOrAmendCase.{AmendCase, CreateCase}
import models.{CreateOrAmendCase, UserAnswers}
import navigation.{AmendNavigator, CreateNavigator, FirstPage}
import pages.CreateOrAmendCasePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CreateOrAmendCaseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateOrAmendCaseController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  amendNavigator: AmendNavigator,
  createNavigator: CreateNavigator,
  formProvider: CreateOrAmendCaseFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CreateOrAmendCaseView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val preparedForm =
        request.userAnswers.getOrElse(UserAnswers(request.identification)).get(CreateOrAmendCasePage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            userAnswers <- Future.fromTry(UserAnswers(request.identification).set(CreateOrAmendCasePage, value))
            _           <- sessionRepository.set(userAnswers)
          } yield Redirect(firstPageForJourney(value, userAnswers))
      )
  }

  private def firstPageForJourney(journey: CreateOrAmendCase, answers: UserAnswers): Call = journey match {
    case CreateCase => createNavigator.nextPage(FirstPage, answers)
    case AmendCase  => amendNavigator.nextPage(FirstPage, answers)
  }

}
