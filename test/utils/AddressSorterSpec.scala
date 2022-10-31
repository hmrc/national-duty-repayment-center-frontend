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

import models.responses.{Location, LookedUpAddress, LookedUpAddressWrapper, Uprn}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AddressSorterSpec extends AnyFreeSpec with Matchers {

  val sorter = new AddressSorter

  "sorting a list of LookedUpAddress objects" - {

    "should sort LookedUpAddress objects by their first 2 lines, starting with the 2nd" in {

      val addrs = List(
        LookedUpAddress(Seq("Flat 1a", "7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2a", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 1", "6 Other Place"), "Anytown", None, "ZZ99 1AA")
      ).map(a => LookedUpAddressWrapper("id", Uprn(123456L), a, "en", Some(Location(0, 0))))

      sorter.sort(addrs).map(_.address) mustBe List(
        LookedUpAddress(Seq("Flat 1", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2a", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 1a", "7 Other Place"), "Anytown", None, "ZZ99 1AA")
      )
    }

    "should sort LookedUpAddress objects by their first 2 lines, starting with the 2nd ignoring text before the number" in {

      val addrs = List(
        LookedUpAddress(Seq("Flat 1", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Unit 2a", "6 Other Place"), "Anytown", None, "ZZ99 1AA")
      ).map(a => LookedUpAddressWrapper("id", Uprn(123456L), a, "en", Some(Location(0, 0))))

      sorter.sort(addrs).map(_.address) mustBe List(
        LookedUpAddress(Seq("Flat 1", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Unit 2a", "6 Other Place"), "Anytown", None, "ZZ99 1AA")
      )
    }

    "should sort LookedUpAddress based on the street number event if there is no number in the house name" in {

      val addrs: Seq[LookedUpAddressWrapper] = List(
        LookedUpAddress(Seq("Little house", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Big house", "7 Other Place"), "Anytown", None, "ZZ99 1AA")
      ).map(a => LookedUpAddressWrapper("id", Uprn(123456L), a, "en", Some(Location(0, 0))))

      sorter.sort(addrs).map(_.address) mustBe List(
        LookedUpAddress(Seq("Little house", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Big house", "7 Other Place"), "Anytown", None, "ZZ99 1AA")
      )
    }

    "should sort LookedUpAddress putting flats with numbers before house names with no numbers" in {

      val addrs = List(
        LookedUpAddress(Seq("Little house", "7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Big house", "7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2a", "7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "7 Other Place"), "Anytown", None, "ZZ99 1AA")
      ).map(a => LookedUpAddressWrapper("id", Uprn(123456L), a, "en", Some(Location(0, 0))))

      val addresses = sorter.sort(addrs)

      addresses.map(_.address) mustBe List(
        LookedUpAddress(Seq("Flat 2a", "7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Big house", "7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Little house", "7 Other Place"), "Anytown", None, "ZZ99 1AA")
      )
    }

    "should sort correctly when there are different numbers of lines" in {

      val addrs = List(
        LookedUpAddress(Seq("9 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2a", "8 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "8 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 8a", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 8b", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2a", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("A House name", "6 Other Place"), "Anytown", None, "ZZ99 1AA")
      ).map(a => LookedUpAddressWrapper("id", Uprn(123456L), a, "en", Some(Location(0, 0))))

      val addresses = sorter.sort(addrs)

      addresses.map(_.address) mustBe List(
        LookedUpAddress(Seq("Flat 2a", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 8a", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 8b", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("A House name", "6 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("7 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2a", "8 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("Flat 2b", "8 Other Place"), "Anytown", None, "ZZ99 1AA"),
        LookedUpAddress(Seq("9 Other Place"), "Anytown", None, "ZZ99 1AA")
      )
    }

    "should sort a complex data set correctly" in {

      val addrs = List(
        LookedUpAddress(List("275 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("277 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("279 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("281 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("283 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("285 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("287 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Beulah Villa", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("First Floor, Lodge Cottage", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Ground Floor, Lodge Cottage", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Homend Lodge", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Ledbury Railway Station", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Little Homend", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Oakland Lodge", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Rothesay", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Station House", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Annexe, The Malt House", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Cottage", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Cottage at Homend Lodge", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(
          List("Unit 1, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 1a-2a,Station Industrial Estat", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 3, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 3a, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(List("Unit 4", "Homend Trading Estate"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(
          List("Unit 4, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 4a, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 5, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 5a, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 6, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(List("Unit 6a", "Homend Trading Estate"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(
          List("Unit 6b, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 6c, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 7, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 7b, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        )
      ).map(a => LookedUpAddressWrapper("id", Uprn(123456L), a, "en", Some(Location(0, 0))))

      val addresses = sorter.sort(addrs)

      addresses.map(_.address) mustBe List(
        LookedUpAddress(
          List("Unit 1a-2a,Station Industrial Estat", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 1, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 3, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 3a, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(List("Unit 4", "Homend Trading Estate"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(
          List("Unit 4, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 4a, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 5, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 5a, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(List("Unit 6a", "Homend Trading Estate"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(
          List("Unit 6, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 6b, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 6c, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 7, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(
          List("Unit 7b, Station Industrial Estate", "The Homend"),
          "ABC Town",
          Some("ABC County"),
          "ZZ99 1AA"
        ),
        LookedUpAddress(List("The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Beulah Villa", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("First Floor, Lodge Cottage", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Ground Floor, Lodge Cottage", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Homend Lodge", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Ledbury Railway Station", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Little Homend", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Oakland Lodge", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Rothesay", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("Station House", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Annexe, The Malt House", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Cottage", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("The Cottage at Homend Lodge", "The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("275 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("277 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("279 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("281 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("283 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("285 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
        LookedUpAddress(List("287 The Homend"), "ABC Town", Some("ABC County"), "ZZ99 1AA")
      )
    }

  }

  "should sort another complex data set correctly" in {

    val addrs = List(
      LookedUpAddress(List("49 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("49-53 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("51-53", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("55 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("57 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59B Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59C Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59b", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("61 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("63 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("65 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Building B and B1,Bush Hill Park Pr", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Building C,Bush Hill Park Primary", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Building D,Bush Hill Park Primary", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Car Park, Wheatsheaf Hall", "121-123 Roman Way"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Classroom Building,Bush Hill Park", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Flat", "55 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Flat", "57 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Infant Building,Bush Hill Park Prim", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Land, Bush Hill Park Primary School", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Main Building,Bush Hill Park Primar", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Main Building, Wheatsheaf Hall", "121-123 Roman Way"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Nursery,Bush Hill Park Primary Scho", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Offices 1", "67A Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Progress Centre,Bush Hill Park Prim", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Rear of 49-53", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Rear of 61", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Reception,Bush Hill Park Primary Sc", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Store F,Bush Hill Park Primary Scho", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Store G,Bush Hill Park Primary Scho", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Studio,Bush Hill Park Primary Schoo", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Unit 1, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 10, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 2, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 3, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 4, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 5, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 6, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 7, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 8, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 9, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Wheatsheaf Hall", "121-123 Roman Way"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Wheatsheaf Hall", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA")
    ).map(a => LookedUpAddressWrapper("id", Uprn(123456L), a, "en", Some(Location(0, 0))))

    val addresses = sorter.sort(addrs)

    addresses.map(_.address) mustBe List(
      LookedUpAddress(List("Unit 1, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Building B and B1,Bush Hill Park Pr", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Unit 2, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 3, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 4, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 5, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 6, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 7, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 8, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 9, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Unit 10, Rear of 59-69", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("49 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("49-53 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Rear of 49-53", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("51-53", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("55 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Flat", "55 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("57 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Flat", "57 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59B Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59C Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("59b", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("61 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Rear of 61", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("63 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("65 Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Offices 1", "67A Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("67a", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(List("Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Building C,Bush Hill Park Primary", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Building D,Bush Hill Park Primary", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Classroom Building,Bush Hill Park", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Infant Building,Bush Hill Park Prim", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Land, Bush Hill Park Primary School", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Main Building,Bush Hill Park Primar", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Nursery,Bush Hill Park Primary Scho", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Progress Centre,Bush Hill Park Prim", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Reception,Bush Hill Park Primary Sc", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Store F,Bush Hill Park Primary Scho", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Store G,Bush Hill Park Primary Scho", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Studio,Bush Hill Park Primary Schoo", "Main Avenue"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Wheatsheaf Hall", "Main Avenue"), "ABC Town", Some("ABC County"), "ZZ99 1AA"),
      LookedUpAddress(
        List("Car Park, Wheatsheaf Hall", "121-123 Roman Way"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(
        List("Main Building, Wheatsheaf Hall", "121-123 Roman Way"),
        "ABC Town",
        Some("ABC County"),
        "ZZ99 1AA"
      ),
      LookedUpAddress(List("Wheatsheaf Hall", "121-123 Roman Way"), "ABC Town", Some("ABC County"), "ZZ99 1AA")
    )
  }

}
