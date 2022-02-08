/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import base.SpecBase
import data.TestData.testClaimReasonTypes
import models.ClaimReasonType
import pages.{ClaimReasonTypeMultiplePage, ClaimReasonTypePage}
import viewmodels.AnswerSection

class CheckYourAnswersHelperSpec extends SpecBase {

  def findRow(sections: Seq[AnswerSection], sectionHeading: String, rowLabel: String) = {

    val section = sections.find(sec => sec.headingKey.contains(messages(sectionHeading)))

    section.flatMap(_.rows.find { row =>
      println("ROW: " + row.label.toString())
      row.label.toString() == messages(rowLabel)
    })
  }

  "CheckYourAnswersHelper" must {

    "include main reason row " when {

      "user has selected multiple claim reasons" in {

        val userAnswers = emptyUserAnswers
          .set(ClaimReasonTypeMultiplePage, testClaimReasonTypes).success.value
          .set(ClaimReasonTypePage, testClaimReasonTypes.head).success.value

        val sections = cyaFactory.instance(userAnswers).getCheckYourAnswerSections

        findRow(
          sections,
          "applicationInformation.checkYourAnswersLabel",
          "claimReasonType.multiple.heading"
        ).isDefined mustBe true

        findRow(
          sections,
          "applicationInformation.checkYourAnswersLabel",
          "claimReasonType.heading"
        ).isDefined mustBe true

      }
    }

    "only include single reason row " when {

      "user has selected single claim reasons" in {

        val singleReason: Set[ClaimReasonType] = Set(ClaimReasonType.Value)

        val userAnswers = emptyUserAnswers
          .set(ClaimReasonTypeMultiplePage, singleReason).success.value

        val sections = cyaFactory.instance(userAnswers).getCheckYourAnswerSections

        findRow(
          sections,
          "applicationInformation.checkYourAnswersLabel",
          "claimReasonType.multiple.heading"
        ).isDefined mustBe true

        findRow(
          sections,
          "applicationInformation.checkYourAnswersLabel",
          "claimReasonType.checkYourAnswersLabel.main"
        ).isDefined mustBe false

      }
    }
  }
}
