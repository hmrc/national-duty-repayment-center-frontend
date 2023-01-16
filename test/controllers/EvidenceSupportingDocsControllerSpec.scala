/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import models.NumberOfEntriesType.{Multiple, Single}
import models.{ClaimReasonType, Entries, UserAnswers}
import pages.{ClaimReasonTypeMultiplePage, ClaimReasonTypePage, NumberOfEntriesTypePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.EvidenceSupportingDocsView

class EvidenceSupportingDocsControllerSpec extends SpecBase {

  "EvidenceSupportingDocs Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.EvidenceSupportingDocsController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[EvidenceSupportingDocsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(Set.empty, None, defaultBackLink)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for each ClaimReasonType" in {

      ClaimReasonType.values.foreach { claimReason =>
        val reasons: Set[ClaimReasonType] = Set(claimReason)
        val userAnswers = UserAnswers(userIdentification).set(ClaimReasonTypeMultiplePage, reasons).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, routes.EvidenceSupportingDocsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EvidenceSupportingDocsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(Set(claimReason), None, defaultBackLink)(request, messages).toString

        application.stop()

      }
    }

    "return OK and the correct view for multiple ClaimReasonTypes" in {

      val reasons: Set[ClaimReasonType] = ClaimReasonType.values.toSet
      val userAnswers = UserAnswers(userIdentification)
        .set(ClaimReasonTypeMultiplePage, reasons).success.value
        .set(ClaimReasonTypePage, reasons.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.EvidenceSupportingDocsController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[EvidenceSupportingDocsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(reasons, None, defaultBackLink)(request, messages).toString

      application.stop()

    }

    "return OK and the correct view for single entry" in {

      val userAnswers =
        UserAnswers(userIdentification).set(NumberOfEntriesTypePage, Entries(Single, None)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.EvidenceSupportingDocsController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) must include(messages("evidenceSupportingDocs.paragraph1.single"))

      application.stop()
    }

    "return OK and the correct view for 'small' multiple entry" in {

      val userAnswers =
        UserAnswers(userIdentification).set(NumberOfEntriesTypePage, Entries(Multiple, Some("10"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.EvidenceSupportingDocsController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) must include(messages("evidenceSupportingDocs.paragraph1.multiple-small"))

      application.stop()
    }

    "return OK and the correct view for 'large' multiple entry" in {

      val userAnswers =
        UserAnswers(userIdentification).set(NumberOfEntriesTypePage, Entries(Multiple, Some("15"))).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, routes.EvidenceSupportingDocsController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) must include(messages("evidenceSupportingDocs.paragraph2.multiple-large"))

      application.stop()
    }
  }
}
