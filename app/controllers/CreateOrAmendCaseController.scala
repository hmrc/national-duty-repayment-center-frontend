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
import forms.CreateOrAmendCaseFormProvider
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages.CreateOrAmendCasePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.CreateOrAmendCaseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateOrAmendCaseController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       formProvider: CreateOrAmendCaseFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: CreateOrAmendCaseView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.internalId)).get(CreateOrAmendCasePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
            for {
              userAnswers <- Future.fromTry (request.userAnswers.getOrElse(UserAnswers(request.internalId)).
                set(CreateOrAmendCasePage, value))
              res <- sessionRepository.set(userAnswers)
              if(res)
            } yield Redirect(navigator.nextPage(CreateOrAmendCasePage, mode, userAnswers))
      )
  }
}
