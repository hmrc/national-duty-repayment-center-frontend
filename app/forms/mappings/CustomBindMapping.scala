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

package forms.mappings

import play.api.data.{FormError, Mapping}
import play.api.data.validation.Constraint

class CustomBindMapping[T](
  fieldName: String,
  fBind: (Map[String, String]) => Either[Seq[FormError], T],
  fUnbind: (T) => Map[String, String]
) extends Mapping[T] {

  override val key: String = fieldName

  override val mappings: Seq[Mapping[_]] = Seq(this)

  override val constraints: Seq[Constraint[T]] = Nil

  override def bind(data: Map[String, String]): Either[Seq[FormError], T] = fBind(data)

  override def unbind(value: T): Map[String, String] = fUnbind(value)

  override def unbindAndValidate(value: T): (Map[String, String], Seq[FormError]) =
    throw new UnsupportedOperationException("unbindAndValidate")

  override def withPrefix(prefix: String): Mapping[T] =
    new CustomBindMapping(prefix + fieldName, fBind, fUnbind)

  override def verifying(constraints: Constraint[T]*): Mapping[T] =
    throw new UnsupportedOperationException("verifying")

}
