package com.selfjourney.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val dbHost = System.getenv("DB_HOST") ?: environment.config.propertyOrNull("database.host")?.getString() ?: "localhost"
    val dbPort = System.getenv("DB_PORT") ?: environment.config.propertyOrNull("database.port")?.getString() ?: "3306"
    val dbName = System.getenv("DB_NAME") ?: environment.config.propertyOrNull("database.name")?.getString() ?: "self_journey"
    val dbUser = System.getenv("DB_USER") ?: environment.config.propertyOrNull("database.user")?.getString() ?: "appuser"
    val dbPassword = System.getenv("DB_PASS") ?: environment.config.propertyOrNull("database.password")?.getString() ?: "apppass"

    val jdbcUrl = "jdbc:mysql://$dbHost:$dbPort/$dbName?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&useUnicode=true"

    log.info("Connecting to database: $jdbcUrl")

    // HikariCP connection pool
    val hikariConfig = HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        username = dbUser
        password = dbPassword
        driverClassName = "com.mysql.cj.jdbc.Driver"
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)

    // Flyway migration
    log.info("Running Flyway migrations...")
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load()

    flyway.migrate()
    log.info("Flyway migrations completed")

    // Exposed database connection
    Database.connect(dataSource)
    log.info("Database connected successfully")
}
