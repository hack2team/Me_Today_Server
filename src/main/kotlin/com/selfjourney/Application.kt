package com.selfjourney

import com.selfjourney.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.config.*

fun main() {
    embeddedServer(
        Netty,
        environment = applicationEngineEnvironment {
            config = HoconApplicationConfig(
                com.typesafe.config.ConfigFactory.load()
            )
            connector {
                port = config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 8080
                host = "0.0.0.0"
            }
        }
    ).start(wait = true)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureOpenAPI()
    configureRouting()
}
