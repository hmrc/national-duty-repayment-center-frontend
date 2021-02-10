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

import akka.actor.{Actor, PoisonPill}
import akka.pattern.pipe
import akka.util.Timeout
import services.FileUploadState

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

case class CheckState2(callbackArrived: Boolean)
case object CallbackArrived
case class StopWaiting(maxWaitTime: LocalDateTime)


class CheckStateActor @Inject()()(implicit ec: ExecutionContext) extends Actor {
  implicit val timeout = Timeout(30 seconds)
  import akka.pattern.ask

  def receive = active(false)

  def active(completed: Boolean): Receive = {
    case CallbackArrived => {
      //println("Callback has arrived")
      context become active(true)
    }

    case StopWaiting(maxWaitTime: LocalDateTime) => {

      if (LocalDateTime.now().isAfter(maxWaitTime) || completed) {
        //println(s"I am done.. you know $completed")
        context.become(active(false))
        Future.successful(completed).pipeTo(sender)
      }
      else {
        context.become(active(false))
        (self ? StopWaiting(maxWaitTime)).pipeTo(sender)
      }
    }.pipeTo(sender)
  }
}