/*
 * Copyright 2021 - 2023 Sporta Technologies PVT LTD & the ZIO HTTP contributors.
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

package zio.http.endpoint

import java.time.Instant

import zio._
import zio.test._

import zio.stream.ZStream

import zio.schema.annotation.validate
import zio.schema.codec.{DecodeError, JsonCodec}
import zio.schema.validation.Validation
import zio.schema.{DeriveSchema, Schema, StandardType}

import zio.http.Header.ContentType
import zio.http.Method._
import zio.http._
import zio.http.codec.HttpCodec.{query, queryInt}
import zio.http.codec._
import zio.http.endpoint._
import zio.http.forms.Fixtures.formField

object EndpointSpec extends ZIOHttpSpec {
  def extractStatus(response: Response): Status = response.status

  case class NewPost(value: String)

  case class User(
    @validate(Validation.greaterThan(0))
    id: Int,
  )

  def spec = suite("DocSpec")(
    suite("documentation")(
      test("??") {
        val endpoint = Endpoint(GET / "users" / int("userId")) ?? Doc.h1("hello") ?? Doc.h2("world")
        assertTrue(endpoint.doc == Doc.h1("hello") + Doc.h2("world"))
      },
    ),
  )

  def testEndpoint[R](service: Routes[R, Nothing])(
    url: String,
    expected: String,
  ): ZIO[R, Response, TestResult] = {
    val request = Request.get(url = URL.decode(url).toOption.get)
    for {
      response <- service.toHttpApp.runZIO(request)
      body     <- response.body.asString.orDie
    } yield assertTrue(body == "\"" + expected + "\"") // TODO: Real JSON Encoding
  }

  final case class ImageMetadata(description: String, createdAt: Instant)
  object ImageMetadata {
    implicit val schema: Schema[ImageMetadata] = DeriveSchema.gen[ImageMetadata]
  }
}
