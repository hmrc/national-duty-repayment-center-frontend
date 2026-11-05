/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.{AmendConfirmationView, ClaimSummaryView, ConfirmationView}

class UserResearchBannerSpec extends SpecBase {

  private def appWith(bannerEnabled: Boolean): Application =
    applicationBuilder().configure("features.user-research-banner" -> bannerEnabled).build()

  private lazy val bannerOn  = appWith(bannerEnabled = true)
  private lazy val bannerOff = appWith(bannerEnabled = false)

  private val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")

  private def messagesFor(app: Application, lang: String): Messages =
    app.injector.instanceOf[MessagesApi].preferred(Seq(Lang(lang)))

  private def optedInPages(app: Application)(implicit messages: Messages): Seq[(String, HtmlFormat.Appendable)] =
    Seq(
      "ConfirmationView (/application-complete)" ->
        app.injector.instanceOf[ConfirmationView].apply("claim-id", Seq.empty)(request, messages),
      "AmendConfirmationView (/amend/information-sent)" ->
        app.injector.instanceOf[AmendConfirmationView].apply("claim-id", Seq.empty)(request, messages)
    )

  private def banner(doc: Document) = doc.getElementsByClass("hmrc-user-research-banner")

  "User research banner" must {

    "be displayed with the English link on every opted-in page when the feature is enabled" in {
      implicit val msgs: Messages = messagesFor(bannerOn, "en")
      optedInPages(bannerOn).foreach { case (name, html) =>
        withClue(name) {
          val doc = Jsoup.parse(html.toString)
          banner(doc).size mustBe 1
          banner(doc).first().parent().tagName() mustBe "header"
          doc.select(".hmrc-user-research-banner__link").attr("href") mustBe "https://banner-en"
          doc.select(".hmrc-user-research-banner__close").size mustBe 0
        }
      }
    }

    "be displayed with the Welsh link and Welsh copy when the page is in Welsh" in {
      implicit val msgs: Messages = messagesFor(bannerOn, "cy")
      optedInPages(bannerOn).foreach { case (name, html) =>
        withClue(name) {
          val doc  = Jsoup.parse(html.toString)
          val link = doc.select(".hmrc-user-research-banner__link")
          link.attr("href") mustBe "https://banner-cy"
          link.text must include("Ymunwch â’n panel ymchwil")
        }
      }
    }

    "not be displayed on any opted-in page when the feature is disabled" in {
      implicit val msgs: Messages = messagesFor(bannerOff, "en")
      optedInPages(bannerOff).foreach { case (name, html) =>
        withClue(name) {
          banner(Jsoup.parse(html.toString)).size mustBe 0
        }
      }
    }

    "not be displayed on a page that does not opt in, even when the feature is enabled" in {
      implicit val msgs: Messages = messagesFor(bannerOn, "en")
      val html = bannerOn.injector.instanceOf[ClaimSummaryView].apply(Seq.empty, "confirmation.summary.title")(
        request,
        msgs
      )
      banner(Jsoup.parse(html.toString)).size mustBe 0
    }
  }
}
