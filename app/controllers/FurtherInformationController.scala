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
import forms.FurtherInformationFormProvider

import javax.inject.Inject
import models.{AmendCaseResponseType, Mode, UserAnswers}
import navigation.Navigator
import pages.{AmendCaseResponseTypePage, FurtherInformationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.FurtherInformationView

import scala.collection.Set
import scala.concurrent.{ExecutionContext, Future}

class FurtherInformationController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              sessionRepository: SessionRepository,
                                              navigator: Navigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: FurtherInformationFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: FurtherInformationView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  private def getBackLink(mode: Mode, hasSupportingDocs: Boolean): Call = {
    if(hasSupportingDocs) routes.AmendCaseSendInformationController.showFileUploaded(mode)
    else  routes.AmendCaseResponseTypeController.onPageLoad(mode)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(FurtherInformationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getBackLink(mode, hasSupportingDocs(request.userAnswers))))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, getBackLink(mode, hasSupportingDocs(request.userAnswers))))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(FurtherInformationPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(FurtherInformationPage, mode, updatedAnswers))
      )
  }

  def hasSupportingDocs(userAnswers: UserAnswers): Boolean  = {
    userAnswers.get(AmendCaseResponseTypePage) match {
      case Some(s) => s.contains(AmendCaseResponseType.SupportingDocuments)
      case _ => false
    }
  }
}
