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
import forms.NumberOfEntriesTypeFormProvider
import javax.inject.Inject
import models.UserAnswers
import navigation.CreateNavigator
import pages.{ClaimantTypePage, NumberOfEntriesTypePage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.NumberOfEntriesTypeView

import scala.concurrent.{ExecutionContext, Future}

class NumberOfEntriesTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: NumberOfEntriesTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NumberOfEntriesTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = NumberOfEntriesTypePage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(NumberOfEntriesTypePage) match {
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
          // TODO - remove this logic of clearing answers if number of entries is changed
          if (
            !request.userAnswers.get(NumberOfEntriesTypePage).contains(value)
            && request.userAnswers.changePage.contains(NumberOfEntriesTypePage.toString)
          ) {
            val claimantType = request.userAnswers.get(ClaimantTypePage).get
            for {
              _           <- sessionRepository.resetData(request.userAnswers)
              sessionData <- sessionRepository.get(request.internalId)
              userAnswers <- Future.fromTry(
                sessionData.map(_.copy(id = request.internalId)).getOrElse(UserAnswers(request.internalId)).set(
                  NumberOfEntriesTypePage,
                  value
                )
              )
              claimantTypeAnswers <- Future.fromTry(userAnswers.set(ClaimantTypePage, claimantType))
              res                 <- sessionRepository.set(claimantTypeAnswers)
            } yield Redirect(nextPage(userAnswers))
          } else
            for {
              userAnswers <- Future.fromTry(request.userAnswers.set(NumberOfEntriesTypePage, value))
              res         <- sessionRepository.set(userAnswers)
            } yield Redirect(nextPage(userAnswers))
      )
  }

}
