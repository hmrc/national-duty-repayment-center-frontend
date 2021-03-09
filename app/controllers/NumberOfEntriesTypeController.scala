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
import forms.{HowManyEntriesFormProvider, NumberOfEntriesTypeFormProvider}

import javax.inject.Inject
import models.{Mode, NoOfEntries, NumberOfEntriesType}
import navigation.Navigator
import pages.{BulkFileUploadPage, HowManyEntriesPage, NumberOfEntriesTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.NumberOfEntriesTypeView

import scala.concurrent.{ExecutionContext, Future}

class NumberOfEntriesTypeController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               sessionRepository: SessionRepository,
                                               navigator: Navigator,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: NumberOfEntriesTypeFormProvider,
                                               entriesFormProvider: HowManyEntriesFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: NumberOfEntriesTypeView
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  val entriesForm = entriesFormProvider()

  private def getBackLink(mode: Mode): Call = {
    routes.ClaimantTypeController.onPageLoad(mode)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(NumberOfEntriesTypePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val numberOfEntriesForm = request.userAnswers.get(HowManyEntriesPage) match {
        case None => entriesForm
        case Some(value) => entriesForm.fill(value)
      }

      Ok(view(preparedForm, numberOfEntriesForm, mode, getBackLink(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, entriesForm, mode, getBackLink(mode)))),

        value =>
          for {
            updatedAnswersWithEntries <- Future.fromTry(request.userAnswers.set (NumberOfEntriesTypePage, value))
            updatedAnswers <-
               Future.fromTry(form.bindFromRequest().data.contains("entries") match {
                case false => updatedAnswersWithEntries.set(NumberOfEntriesTypePage, value)
                case true => updatedAnswersWithEntries.set(HowManyEntriesPage,
                  NoOfEntries(form.bindFromRequest().data.get("entries").head))
              })
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(NumberOfEntriesTypePage, mode, updatedAnswers))
      )
  }
}

