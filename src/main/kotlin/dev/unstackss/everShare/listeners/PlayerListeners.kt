package dev.unstackss.everShare.listeners


import dev.unstackss.everShare.EverShare
import dev.unstackss.everShare.referral.ReferralManager
import dev.unstackss.everShare.utils.Format
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.util.Vector

class PlayerListeners : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val name = player.name

        val referralManager = ReferralManager()

        val existingReferral = referralManager.getReferralForPlayer(uuid)
        if (existingReferral == null) {
            val referralCode = referralManager.createReferral(uuid, name)
            player.sendMessage(Format.color(EverShare.plugin.getMessage("referral_created_message", "referral_code" to referralCode)))
        }
    }
}
