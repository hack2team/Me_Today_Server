package com.selfjourney

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ApplicationTest : StringSpec({
    "health endpoint returns OK" {
        testApplication {
            application {
                module()
            }

            val response = client.get("/health")
            response.status shouldBe HttpStatusCode.OK

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            json["status"]?.jsonPrimitive?.content shouldBe "healthy"
        }
    }

    "can retrieve all users" {
        testApplication {
            application {
                module()
            }

            val response = client.get("/api/users")
            response.status shouldBe HttpStatusCode.OK

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            json["success"]?.jsonPrimitive?.content shouldBe "true"
        }
    }

    "can create a new user" {
        testApplication {
            application {
                module()
            }

            val response = client.post("/api/users") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "name": "Test User",
                        "age": 25,
                        "gender": "other",
                        "email": "test@example.com"
                    }
                """.trimIndent())
            }

            response.status shouldBe HttpStatusCode.Created

            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            json["success"]?.jsonPrimitive?.content shouldBe "true"
            json["data"] shouldNotBe null
        }
    }
})
