package dev.unstackss.everShare.referral

import dev.unstackss.everShare.database.Referrals
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.random.Random

class ReferralManager {
    fun generateReferralCode(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random
        return (1..8)
            .map { characters[random.nextInt(characters.length)] }
            .joinToString("")
    }

    fun createReferral(playerUuid: UUID, playerName: String): String {
        var referralCode = generateReferralCode()
        transaction {
            while (Referrals.select { Referrals.referral eq referralCode }.singleOrNull() != null) {
                referralCode = generateReferralCode()
            }
            Referrals.insert {
                it[uuid] = playerUuid
                it[name] = playerName
                it[referral] = referralCode
                it[totalHits] = 0
            }
        }
        return referralCode
    }
    fun getReferral(code: String): Referral? {
        return transaction {
            Referrals.select { Referrals.referral eq code }.singleOrNull()?.let {
                Referral(
                    uuid = it[Referrals.uuid],
                    name = it[Referrals.name],
                    referral = it[Referrals.referral],
                    totalHits = it[Referrals.totalHits]
                )
            }
        }
    }
    fun getReferralForPlayer(playerUuid: UUID): Referral? {
        return transaction {
            Referrals.select { Referrals.uuid eq playerUuid }.singleOrNull()?.let {
                Referral(
                    uuid = it[Referrals.uuid],
                    name = it[Referrals.name],
                    referral = it[Referrals.referral],
                    totalHits = it[Referrals.totalHits]
                )
            }
        }
    }
    fun incrementReferralHits(code: String) {
        transaction {
            Referrals.update({ Referrals.referral eq code }) {
                it[totalHits] = SqlExpressionBuilder.run { totalHits + 1 }
            }
        }
    }
    data class Referral(
        val uuid: UUID,
        val name: String,
        val referral: String,
        val totalHits: Int
    )
}
