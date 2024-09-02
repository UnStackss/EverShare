@file:Suppress("DEPRECATION")

package dev.unstackss.everShare

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.unstackss.everShare.commands.ReferralInfoCommand
import dev.unstackss.everShare.database.DatabaseManager
import dev.unstackss.everShare.listeners.PlayerListeners
import dev.unstackss.everShare.web.WebServer
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.properties.Delegates

class EverShare : JavaPlugin() {
    private lateinit var webServer: WebServer
    lateinit var messages: JsonObject

    companion object {
        var plugin: EverShare by Delegates.notNull()
    }

    private fun initalize() {
        plugin = this
        saveDefaultConfig()
        createDefaultLanguageFileIfNotExists(dataFolder)
        loadLanguageFile(config.getString("messages.language") ?: "en_us")

        DatabaseManager.init(
            url = config.getString("database.url") ?: "jdbc:mysql://localhost:3306/minecraft",
            user = config.getString("database.user") ?: "root",
            password = config.getString("database.password") ?: "password"
        )
        webServer = WebServer(this)
        webServer.start()
        logger.info("EverShare plugin successfully enabled!")
    }

    private fun events() {
        server.pluginManager.registerEvents(PlayerListeners(), this)
    }

    private fun commands() {
        getCommand("invite")!!.setExecutor(ReferralInfoCommand())
    }

    override fun onEnable() {
        initalize()
        events()
        commands()
        downloadFavicon()
    }

    override fun onDisable() {
        DatabaseManager.close()
        webServer.stop()
        logger.info("EverShare plugin successfully disabled!")
    }

    private fun loadLanguageFile(languageCode: String) {
        val languageFile = File(dataFolder, "languages/$languageCode.json")
        if (!languageFile.exists()) {
            languageFile.parentFile.mkdirs()
            saveResource("languages/$languageCode.json", false)
        }

        val parser = JsonParser()
        try {
            messages = parser.parse(FileReader(languageFile)) as JsonObject
        } catch (e: Exception) {
            logger.severe("Error loading language file: ${e.message}")
        }
    }

    fun getMessage(key: String, vararg replacements: Pair<String, String>): String {
        var message = messages[key]?.asString ?: key
        replacements.forEach { (placeholder, value) ->
            message = message.replace("{$placeholder}", value)
        }
        return message
    }

    fun createDefaultLanguageFileIfNotExists(pluginDir: File) {
        val languageFile = File(pluginDir, "languages/en_us.json")

        if (!languageFile.exists()) {
            try {
                languageFile.parentFile.mkdirs()
                languageFile.createNewFile()
                val defaultMessages = JsonObject().apply {
                    addProperty("referral_page_title", "Referral Information")
                    addProperty("referral_owner", "This referral code belongs to {player_name}")
                    addProperty("referral_total_hits", "Total users who have opened this link: {total_hits}")
                    addProperty("server_ip_title", "Server IP")
                    addProperty("server_ip", "{server_ip}")
                    addProperty("referral_not_found", "Referral code not found.")
                    addProperty("referral_created_message", "&eReferral created successfully your referral code is &6{referral_code}")
                    addProperty("referral_code", "&eReferral Code: &6{referral_code}")
                    addProperty("referral_code_site", "Referral Code: {referral_code}")
                    addProperty("referral_link", "&eReferral Link: &6{referral_link}")
                    addProperty("total_hits", "&eTotal Hits: &6{total_hits}")
                    addProperty("total_hits_site", "Total Hits: {total_hits}")
                    addProperty("no_referral_found", "&cNo referral found for your account.")
                    addProperty("no_permission", "&cYou do not have permission to use this command.")
                    addProperty("players_only", "&cThis command can only be used by players.")
                    addProperty("copy_ip", "Copy IP")
                }
                FileWriter(languageFile).use { writer ->
                    Gson().toJson(defaultMessages, writer)
                }
                logger.info("Default language file created at ${languageFile.absolutePath}")
            } catch (e: IOException) {
                logger.severe("Error creating default language file: ${e.message}")
            }
        } else {
            logger.info("Language file already exists at ${languageFile.absolutePath}")
        }
    }

    private fun downloadFavicon() {
        val faviconFile = File(dataFolder, "favicon.ico")
        val faviconUrl = config.getString("webserver.favicon_url")

        if (!faviconFile.exists() && faviconUrl != null) {
            try {
                downloadFile(faviconUrl, faviconFile)
            } catch (_: Exception) {}
        }
    }

    private fun downloadFile(fileUrl: String, outputFile: File) {
        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            connection.inputStream.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
