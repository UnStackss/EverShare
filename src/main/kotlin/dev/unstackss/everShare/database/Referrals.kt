package dev.unstackss.everShare.database

import org.jetbrains.exposed.sql.Table

object Referrals : Table() {
    val uuid = uuid("uuid").uniqueIndex()
    val name = varchar("name", 16)
    val referral = varchar("referral", 8).uniqueIndex()
    val totalHits = integer("totalHits").default(0)
}