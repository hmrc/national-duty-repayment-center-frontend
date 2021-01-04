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
import javax.inject.Inject
import models.{ArticleType, CustomsRegulationType, Mode}
import navigation.Navigator
import pages.{ArticleTypePage, BulkFileUploadPage, CustomsRegulationTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.BulkFileUploadView

import scala.concurrent.{ExecutionContext, Future}

class BulkFileUploadController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: BulkFileUploadView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      Ok(view())
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
        val value = request.userAnswers.get(CustomsRegulationTypePage).get

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomsRegulationTypePage, value))
            updatedAnswersWithArticleType <- {
              updatedAnswers.get(CustomsRegulationTypePage) match {
                case Some(CustomsRegulationType.UKCustomsCodeRegulation) => Future.fromTry(updatedAnswers.set(ArticleTypePage, ArticleType.Schedule))
                case _ => Future.fromTry(request.userAnswers.set(CustomsRegulationTypePage, value))
              }
            }
            _              <- sessionRepository.set(updatedAnswersWithArticleType)
          } yield Redirect(navigator.nextPage(BulkFileUploadPage, mode, updatedAnswersWithArticleType))
  }
}
