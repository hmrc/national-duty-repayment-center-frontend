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
import forms.DoYouOwnTheGoodsFormProvider
import javax.inject.Inject
import models.{DoYouOwnTheGoods, UserAnswers}
import navigation.CreateNavigator
import pages.{DeclarantNamePage, DoYouOwnTheGoodsPage, ImporterNamePage, Page}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DoYouOwnTheGoodsView

import scala.concurrent.{ExecutionContext, Future}

class DoYouOwnTheGoodsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DoYouOwnTheGoodsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DoYouOwnTheGoodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = DoYouOwnTheGoodsPage
  val form                = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val declarantName = request.userAnswers.get(DeclarantNamePage).map(_.toString).getOrElse("")
      val preparedForm = request.userAnswers.get(DoYouOwnTheGoodsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, backLink(request.userAnswers), declarantName))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val declarantName = request.userAnswers.get(DeclarantNamePage).map(_.toString).getOrElse("")

      form.bindFromRequest().fold(
        formWithErrors => {
          val errors: Seq[FormError] =
            formWithErrors.errors.headOption.map(x =>
              Seq(x.copy(messages = Seq(formWithErrors.errors.head.message), args = Seq(declarantName)))
            ).getOrElse(Nil)
          Future.successful(
            BadRequest(view(formWithErrors.copy(errors = errors), backLink(request.userAnswers), declarantName))
          )
        },
        value =>
          for {
            updatedAnswers <- {
              if (value.equals(DoYouOwnTheGoods.Yes))
                Future.fromTry(request.userAnswers.remove(ImporterNamePage).flatMap(_.set(DoYouOwnTheGoodsPage, value)))
              else Future.fromTry(request.userAnswers.set(DoYouOwnTheGoodsPage, value))
            }
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(nextPage(updatedAnswers))
      )
  }

}
