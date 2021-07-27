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
import models.UserAnswers
import navigation.CreateNavigator
import pages.{ClaimReasonTypePage, EvidenceSupportingDocsPage, NumberOfEntriesTypePage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EvidenceSupportingDocsView

import scala.concurrent.ExecutionContext

class EvidenceSupportingDocsController @Inject() (
  override val messagesApi: MessagesApi,
  val navigator: CreateNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: EvidenceSupportingDocsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Navigation[UserAnswers] {

  override val page: Page = EvidenceSupportingDocsPage

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(
        view(
          request.userAnswers.get(ClaimReasonTypePage),
          request.userAnswers.get(NumberOfEntriesTypePage),
          backLink(request.userAnswers)
        )
      )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(nextPage(request.userAnswers))
  }

}
