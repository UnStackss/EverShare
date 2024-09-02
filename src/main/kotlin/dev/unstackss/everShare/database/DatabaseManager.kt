package dev.unstackss.everShare.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {
    private var db: Database? = null

    fun init(url: String, user: String, password: String) {
        db = Database.connect(url, driver = "com.mysql.cj.jdbc.Driver", user = user, password = password)
        transaction(db) {
            SchemaUtils.create(Referrals)
        }
    }

    fun close() {
        db?.connector()?.close()
    }
}