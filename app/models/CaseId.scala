/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.Format

final case class CaseId(value: String)

object CaseId {
  implicit val format: Format[CaseId] = JsonFormatUtils.stringFormat(CaseId.apply)(_.value)
}
