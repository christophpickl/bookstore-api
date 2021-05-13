package com.github.cpickl.bookstore.adapter.jpa

import mu.KotlinLogging.logger
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.sql.DriverManager
import javax.annotation.PostConstruct
import javax.sql.DataSource

@Component
class JpaLogger(
    private val ds: DataSource,
    private val env: Environment,
) {

    private val log = logger {}

    @Suppress("unused")
    @PostConstruct
    fun logDatasource() {
        log.info {
            val meta = ds.connection.metaData
            "Datasource:\n" +
                    listOf(
                        "Url" to meta.url,
                        "Driver" to "${meta.driverName} (${meta.driverVersion})",
                        "Driver class" to "${DriverManager.getDriver(meta.url)::class.qualifiedName}",
                        "Connection class" to "${ds.connection::class.qualifiedName}",
                        "DB Version" to "${meta.databaseMajorVersion}.${meta.databaseMinorVersion}",
                        "Username" to meta.userName,
                        "DDL-auto" to env.getProperty("spring.jpa.hibernate.ddl-auto"),
                    ).joinToString("\n") { "\t${it.first}: ${it.second}" }
        }
    }
}
