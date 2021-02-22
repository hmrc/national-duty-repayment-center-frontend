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
import org.slf4j.LoggerFactory
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.ClaimIdQuery
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.AmendConfirmationView


import scala.concurrent.ExecutionContext
class AmendConfirmationController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: AmendConfirmationView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val successfulResult = for {
        id <- request.userAnswers.get(ClaimIdQuery)
      } yield Ok(view(id))
      successfulResult getOrElse {
        logger.warn("Could not find the registrationId or registrationDate in user answers")
        InternalServerError
      }
  }
}