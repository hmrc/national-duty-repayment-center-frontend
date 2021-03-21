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
import forms.ClaimantTypeFormProvider

import javax.inject.Inject
import models.{CheckMode, Entries, Mode, NormalMode, NumberOfEntriesType, UserAnswers}
import navigation.Navigator
import pages.{ClaimantTypePage, NumberOfEntriesTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ClaimantTypeView

import scala.concurrent.{ExecutionContext, Future}

class ClaimantTypeController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       formProvider: ClaimantTypeFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ClaimantTypeView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.internalId)).get(ClaimantTypePage) match {
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
          if(request.userAnswers.nonEmpty && request.userAnswers.get.get(ClaimantTypePage).get != value
            && mode.equals(CheckMode) ) {
            for {
              _ <- sessionRepository.resetData(request.userAnswers.get)
              sessionData <- sessionRepository.get(request.internalId)
              userAnswers <- Future.fromTry (sessionData.map(_.copy(id = request.internalId)).
                getOrElse(UserAnswers(request.internalId)).set(ClaimantTypePage, value))
              res <- sessionRepository.set(userAnswers)
              if(res)
            } yield Redirect(navigator.nextPage(ClaimantTypePage, NormalMode, userAnswers))
          } else {
            for {
              userAnswers <- Future.fromTry (request.userAnswers.getOrElse(UserAnswers(request.internalId)).
                set(ClaimantTypePage, value))
              res <- sessionRepository.set(userAnswers)
              if(res)
            } yield Redirect(navigator.nextPage(ClaimantTypePage, mode, userAnswers))
          }
      )
  }
}
