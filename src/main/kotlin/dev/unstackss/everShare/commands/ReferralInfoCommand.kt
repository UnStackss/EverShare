package dev.unstackss.everShare.commands

import dev.unstackss.everShare.EverShare.Companion.plugin
import dev.unstackss.everShare.referral.ReferralManager
import dev.unstackss.everShare.utils.Format
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ReferralInfoCommand : CommandExecutor {

    private val referralManager = ReferralManager()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("referralinfo.use")) {
                val playerUuid = sender.uniqueId
                val referral = referralManager.getReferralForPlayer(playerUuid)

                if (referral != null) {
                    val referralCode = referral.referral
                    val hits = referral.totalHits
                    val baseUrl = plugin.config.getString("server.dns") ?: "http://localhost"
                    val referralLink = "$baseUrl/referral/$referralCode"
                    sender.sendMessage(Format.color(plugin.getMessage("referral_code", "referral_code" to referralCode)))
                    sender.sendMessage(Format.color(plugin.getMessage("referral_link", "referral_link" to referralLink)))
                    sender.sendMessage(Format.color(plugin.getMessage("total_hits", "total_hits" to hits.toString())))
                } else {
                    sender.sendMessage(Format.color(plugin.getMessage("no_referral_found")))
                }
            } else {
                sender.sendMessage(Format.color(plugin.getMessage("no_permission")))
            }
        } else {
            sender.sendMessage(Format.color(plugin.getMessage("players_only")))
        }
        return true
    }
}
