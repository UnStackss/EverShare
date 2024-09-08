package dev.unstackss.everShare.web

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import dev.unstackss.everShare.EverShare
import dev.unstackss.everShare.referral.ReferralManager
import spark.Spark
import spark.Spark.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class WebServer(private val plugin: EverShare) {

    private val referralManager = ReferralManager()

    fun start() {
        val host = plugin.config.getString("webserver.host") ?: "localhost"
        val port = plugin.config.getInt("webserver.port", 8081)

        ipAddress(host)
        port(port)
        staticFiles.location("/public")

        val pluginDataFolder = plugin.dataFolder.absolutePath
        staticFiles.externalLocation(pluginDataFolder)

        get("/favicon.ico") { _, res ->
            res.type("image/x-icon")
            plugin.dataFolder.resolve("favicon.ico").inputStream().readBytes()
        }

        get("/referral/:code") { req, res ->
            handleReferral(req, res)
        }

        get("/qrcode/:code") { req, res ->
            handleQRCode(req, res)
        }
    }

    fun stop() {
        Spark.stop()
    }

    private fun handleReferral(req: spark.Request, res: spark.Response): String {
        val code = req.params(":code")
        val referral = referralManager.getReferral(code)

        if (referral != null) {
            referralManager.incrementReferralHits(code)

            val logoUrl = plugin.config.getString("webserver.logo_url") ?: ""
            val siteDescription = plugin.config.getString("webserver.description") ?: ""
            val serverIp = plugin.config.getString("server.ip") ?: "Unknown IP"
            val faviconPath = "/favicon.ico"

            return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${plugin.getMessage("referral_page_title")}</title>
                <link rel="icon" href="$faviconPath" type="image/x-icon">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
                <style>
                    body {
                        background-color: #0D1117;
                        color: white;
                        font-family: 'Arial', sans-serif;
                        margin: 0;
                        padding: 0;
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        text-align: center;
                    }
                    .container {
                        width: 90%;
                        max-width: 600px;
                        margin: auto;
                    }
                    .logo {
                        margin-bottom: 20px;
                    }
                    .card {
                        background-color: #161b22;
                        border-radius: 8px;
                        padding: 20px;
                        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                        text-align: center;
                        animation: fadeIn 1s ease-in-out;
                    }
                    .card h2 {
                        margin: 10px 0;
                        font-size: 24px;
                    }
                    .card p {
                        margin: 10px 0;
                        font-size: 18px;
                    }
                    .referral-code {
                        font-weight: bold;
                        background-color: #0d6efd;
                        color: white;
                        padding: 10px;
                        border-radius: 5px;
                        display: inline-block;
                    }
                    .server-ip {
                        margin-top: 20px;
                        padding: 10px;
                        background-color: #161b22;
                        border-radius: 8px;
                        animation: fadeIn 1s ease-in-out;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        gap: 10px;
                    }
                    .copy-button {
                        background: #0d6efd;
                        border: none;
                        color: white;
                        padding: 10px;
                        border-radius: 5px;
                        cursor: pointer;
                        font-size: 16px;
                    }
                    .copy-button i {
                        margin-right: 5px;
                    }
                    .qrcode-card {
                        width: 97%;
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                        margin-top: 20px;
                        padding: 10px;
                        background-color: #161b22;
                        border-radius: 8px;
                        position: relative;
                    }
                    .qrcode-card img {
                        max-width: 200px;
                        height: auto;
                        display: none;
                    }
                    .loading-spinner {
                        border: 4px solid #f3f3f3;
                        border-top: 4px solid #0d6efd;
                        border-radius: 50%;
                        width: 40px;
                        height: 40px;
                        animation: spin 1s linear infinite;
                        margin: auto;
                    }
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                </style>
                <script>
                    document.addEventListener('DOMContentLoaded', function() {
                        var qrImage = document.querySelector('.qrcode-card img');
                        var spinner = document.querySelector('.loading-spinner');
                        
                        qrImage.onload = function() {
                            spinner.style.display = 'none';
                            qrImage.style.display = 'block';
                        };

                        qrImage.onerror = function() {
                            spinner.style.display = 'none';
                            alert("Errore nel caricamento del QR code. Riprova.");
                        };
                    });
                </script>
            </head>
            <body>
                <div class="container">
                    <div class="logo">
                        <img src="$logoUrl" alt="Logo" style="max-width: 150px; height: auto;">
                        <meta name="description" content="$siteDescription">
                    </div>
                    <div class="card">
                        <h2>${plugin.getMessage("referral_owner", "player_name" to referral.name)}</h2>
                        <p class="referral-code">${
                plugin.getMessage(
                    "referral_code_site",
                    "referral_code" to referral.referral
                )
            }</p>
                        <p>${plugin.getMessage("total_hits_site", "total_hits" to referral.totalHits.toString())}</p>
                    </div>
                    <div class="server-ip">
                        <p>${plugin.getMessage("server_ip_title")}: ${serverIp}</p>
                        <button class="copy-button" onclick="copyToClipboard('${serverIp}')">
                            <i class="fas fa-copy"></i>${plugin.getMessage("copy_ip")}
                        </button>
                    </div>
                    <div class="qrcode-card">
                        <h3>Scan and Share</h3>
                        <div class="loading-spinner"></div>
                        <img src="/qrcode/$code" alt="QR Code">
                    </div>
                </div>
            </body>
            </html>
            """.trimIndent()
        } else {
            res.status(404)
            return plugin.getMessage("referral_not_found")
        }
    }

    private fun handleQRCode(req: spark.Request, res: spark.Response): ByteArray {
        val code = req.params(":code")
        val referralPageUrl = "${req.scheme()}://${req.host()}/referral/$code"

        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(referralPageUrl, BarcodeFormat.QR_CODE, 200, 200)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val qrColor = 0xFF0D6EFD.toInt()
            val backgroundColor = 0x00FFFFFF

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val color = if (bitMatrix.get(x, y)) qrColor else backgroundColor
                    image.setRGB(x, y, color)
                }
            }

            val outputStream = ByteArrayOutputStream()
            ImageIO.write(image, "png", outputStream)
            res.type("image/png")
            outputStream.toByteArray()
        } catch (_: WriterException) {
            res.status(500)
            ByteArray(0)
        }
    }
}
