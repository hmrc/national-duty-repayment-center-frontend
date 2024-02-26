/*
 * Copyright 2024 HM Revenue & Customs
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

package views.components

import javax.inject.{Inject, Singleton}

@Singleton
class html @Inject() (
  val h1: views.html.components.h1,
  val h2: views.html.components.h2,
//  val h3: views.html.components.h3,
  val p: views.html.components.p,
//  val strong: views.html.components.strong,
  val a: views.html.components.link,
  val ul: views.html.components.bullets,
//  val ol: views.html.components.orderedList,
  val button: views.html.components.button
)
