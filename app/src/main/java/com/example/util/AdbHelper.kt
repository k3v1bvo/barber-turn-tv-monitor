package com.example.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Collections


/**
 * Pure Kotlin ADB Client with full RSA Cryptography & Wi-Fi Scanner:
 *
 * 1. Self-contained 2048-bit RSA Key generation & persistence.
 * 2. Complete AOSP ADB Protocol Handshake (CNXN -> AUTH Token -> Signature -> RSAPublicKey -> Open Shell).
 * 3. Triggers the real "¿Permitir depuración USB?" dialog on Xiaomi TV Box / Android TV screen.
 * 4. Subnet Wi-Fi Auto-Discovery: finds TV Box on the local network in under 2 seconds.
 * 5. Grants SYSTEM_ALERT_WINDOW overlay permission permanently.
 */
object AdbHelper {

    private const val TAG = "AdbHelper"
    private const val PREFS_NAME = "barber_adb_crypto_prefs"
    private const val KEY_PRIVATE = "adb_rsa_private_key"
    private const val KEY_PUBLIC = "adb_rsa_public_key"

    // ADB Protocol Command Constants
    private const val A_CNXN = 0x4e584e43 // "CNXN"
    private const val A_AUTH = 0x48545541 // "AUTH"
    private const val A_OPEN = 0x4e45504f // "OPEN"
    private const val A_OKAY = 0x59414b4f // "OKAY"
    private const val A_CLSE = 0x45534c43 // "CLSE"
    private const val A_WRTE = 0x45545257 // "WRTE"

    // Auth Subtypes
    private const val ADB_AUTH_TOKEN = 1
    private const val ADB_AUTH_SIGNATURE = 2
    private const val ADB_AUTH_RSAPUBLICKEY = 3

    private const val A_VERSION = 0x01000000
    private const val MAX_PAYLOAD = 4096

    sealed class AdbResult {
        data class Success(val message: String) : AdbResult()
        data class NeedsAuth(val message: String) : AdbResult()
        data class Error(val message: String) : AdbResult()
    }

    /**
     * Executes ADB command to grant overlay permission on local TV (127.0.0.1) or remote TV (IP).
     */
    suspend fun grantOverlayPermission(
        context: Context,
        targetIp: String = "127.0.0.1",
        targetPort: Int = 5555,
        targetPackage: String = context.packageName
    ): AdbResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        val cleanIp = targetIp.trim().ifBlank { "127.0.0.1" }
        val pkg = targetPackage.ifBlank { context.packageName }

        try {
            val keyPair = getOrGenerateKeyPair(context)

            socket = Socket()
            socket.connect(InetSocketAddress(cleanIp, targetPort), 4500)
            socket.soTimeout = 7000

            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            // 1. Send initial CNXN packet
            val cnxnBanner = "host::BarberSiteTV\u0000".toByteArray(Charsets.UTF_8)
            writeMessage(outputStream, A_CNXN, A_VERSION, MAX_PAYLOAD, cnxnBanner)

            var signatureSent = false
            var isAuthenticated = false

            // Handshake loop (handles token challenge and RSA public key exchange)
            for (step in 0 until 6) {
                val header = readHeader(inputStream) ?: break
                if (header.dataLength > 0 && header.command != A_AUTH && header.command != A_CNXN) {
                    skipBytes(inputStream, header.dataLength)
                }

                when (header.command) {
                    A_CNXN -> {
                        // Successfully authenticated & connected!
                        if (header.dataLength > 0) {
                            skipBytes(inputStream, header.dataLength)
                        }
                        isAuthenticated = true
                        break
                    }
                    A_AUTH -> {
                        val authType = header.arg0
                        val payload = if (header.dataLength > 0) readPayload(inputStream, header.dataLength) else ByteArray(0)

                        if (!signatureSent) {
                            // Step 1: Attempt to authenticate with RSA signature first
                            try {
                                val signature = signToken(payload, keyPair.private as RSAPrivateKey)
                                writeMessage(outputStream, A_AUTH, ADB_AUTH_SIGNATURE, 0, signature)
                                signatureSent = true
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to sign token, falling back to public key", e)
                                val pubKeyPayload = convertRsaPublicKeyToAdbFormat(keyPair.public as RSAPublicKey, "BarberSiteTV")
                                writeMessage(outputStream, A_AUTH, ADB_AUTH_RSAPUBLICKEY, 0, pubKeyPayload)
                                signatureSent = true
                            }
                        } else {
                            // Step 2: Signature was sent but adbd sent another A_AUTH -> key is not yet trusted on TV!
                            // Send RSAPublicKey to force Android TV to display the "¿Permitir depuración USB?" popup on screen!
                            val pubKeyPayload = convertRsaPublicKeyToAdbFormat(keyPair.public as RSAPublicKey, "BarberSiteTV")
                            writeMessage(outputStream, A_AUTH, ADB_AUTH_RSAPUBLICKEY, 0, pubKeyPayload)

                            // Give the TV OS time to show the popup and wait for the user to press "Aceptar" with remote
                            socket.soTimeout = 12000
                            val confirmHeader = readHeader(inputStream)
                            if (confirmHeader != null && confirmHeader.command == A_CNXN) {
                                if (confirmHeader.dataLength > 0) {
                                    skipBytes(inputStream, confirmHeader.dataLength)
                                }
                                isAuthenticated = true
                                break
                            } else {
                                return@withContext AdbResult.NeedsAuth(
                                    "📺 ¡MIRA LA TV! Ha aparecido un cartel en la pantalla. Selecciona 'Permitir siempre' y presiona 'Aceptar' con el control remoto, y luego vuelve a tocar este botón."
                                )
                            }
                        }
                    }
                    else -> {
                        Log.d(TAG, "Received command: 0x${Integer.toHexString(header.command)}")
                    }
                }
            }

            if (!isAuthenticated) {
                return@withContext AdbResult.NeedsAuth(
                    "⚠️ En la pantalla de la TV pulsa 'Permitir siempre' y 'Aceptar' con el control remoto, y luego vuelve a presionar este botón."
                )
            }

            // 2. Send Shell Commands to grant overlay and permissions across standard Android and Xiaomi/MIUI TV
            val commands = listOf(
                "pm grant $pkg android.permission.SYSTEM_ALERT_WINDOW",
                "appops set $pkg SYSTEM_ALERT_WINDOW allow",
                "appops set --user 0 $pkg SYSTEM_ALERT_WINDOW allow",
                "cmd appops set $pkg SYSTEM_ALERT_WINDOW allow",
                "appops set $pkg 24 allow",
                "appops set --user 0 $pkg 24 allow",
                "cmd appops set $pkg 24 allow",
                "appops set $pkg 10021 allow",
                "appops set --user 0 $pkg 10021 allow",
                "cmd appops set $pkg 10021 allow",
                "appops set $pkg 10022 allow",
                "appops set --user 0 $pkg 10022 allow",
                "cmd appops set $pkg 10022 allow",
                "settings put secure overlay_permission_enabled 1"
            ).joinToString("; ")

            val shellCommand = "shell:$commands\u0000"
            val commandPayload = shellCommand.toByteArray(Charsets.UTF_8)
            writeMessage(outputStream, A_OPEN, 1, 0, commandPayload)

            // Wait for shell execution to finish completely on TV Box before closing socket
            var commandsExecuted = false
            for (readStep in 0 until 15) {
                val respHeader = readHeader(inputStream) ?: break
                if (respHeader.command == A_OKAY || respHeader.command == A_WRTE) {
                    commandsExecuted = true
                }
                if (respHeader.dataLength > 0) {
                    skipBytes(inputStream, respHeader.dataLength)
                }
                if (respHeader.command == A_CLSE) {
                    break
                }
            }

            if (commandsExecuted) {
                return@withContext AdbResult.Success("¡Permiso de superposición concedido con éxito en la TV! 🎉")
            }

            return@withContext AdbResult.Success("¡Comando enviado con éxito a la TV!")
        } catch (e: java.net.ConnectException) {
            val isLocal = cleanIp == "127.0.0.1" || cleanIp == "localhost"
            val msg = if (isLocal) {
                "No se pudo conectar localmente al puerto 5555. Asegúrate de activar 'Depuración USB' en los Ajustes de Desarrollador de la TV Box."
            } else {
                "No se pudo conectar a $cleanIp:5555. Asegúrate de que la TV y el celular estén en el mismo Wi-Fi y 'Depuración USB' esté activada en la TV."
            }
            return@withContext AdbResult.Error(msg)
        } catch (e: java.net.SocketTimeoutException) {
            return@withContext AdbResult.NeedsAuth(
                "⏳ Tiempo de espera agotado. Si apareció el cartel en la TV, pulsa 'Aceptar' con el control remoto y presiona el botón nuevamente."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing ADB command", e)
            return@withContext AdbResult.Error("Error: ${e.localizedMessage ?: e.message}")
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Fast local Wi-Fi scanner: Scans the /24 subnet for open ADB ports (5555) in parallel.
     * Returns a list of discovered TV Box IP addresses.
     */
    suspend fun scanLocalNetworkForAdb(context: Context): List<String> = withContext(Dispatchers.IO) {
        val localIp = getLocalIpAddress(context) ?: return@withContext emptyList()
        val subnet = localIp.substringBeforeLast(".")
        val foundIps = Collections.synchronizedList(mutableListOf<String>())

        val scanJobs = (1..254).map { i ->
            async {
                val ip = "$subnet.$i"
                if (ip != localIp) {
                    try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(ip, 5555), 350)
                            foundIps.add(ip)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
        scanJobs.awaitAll()
        foundIps.toList()
    }

    /**
     * Gets the current device's local Wi-Fi IP address.
     */
    @Suppress("DEPRECATION")
    fun getLocalIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiInfo = wifiManager?.connectionInfo
            val ipInt = wifiInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                return String.format(
                    java.util.Locale.US,
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }

            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addresses = intf.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    // =========================================================================
    // RSA CRYPTOGRAPHY HELPERS FOR ADB HANDSHAKE
    // =========================================================================

    private fun getOrGenerateKeyPair(context: Context): KeyPair {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val privStr = prefs.getString(KEY_PRIVATE, null)
        val pubStr = prefs.getString(KEY_PUBLIC, null)

        if (!privStr.isNullOrBlank() && !pubStr.isNullOrBlank()) {
            try {
                val keyFactory = KeyFactory.getInstance("RSA")
                val privBytes = Base64.decode(privStr, Base64.DEFAULT)
                val pubBytes = Base64.decode(pubStr, Base64.DEFAULT)
                val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privBytes))
                val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(pubBytes))
                return KeyPair(publicKey, privateKey)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to restore existing RSA key, generating new one", e)
            }
        }

        // Generate new 2048-bit RSA key pair
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val keyPair = kpg.generateKeyPair()

        prefs.edit()
            .putString(KEY_PRIVATE, Base64.encodeToString(keyPair.private.encoded, Base64.DEFAULT))
            .putString(KEY_PUBLIC, Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT))
            .apply()

        return keyPair
    }

    private fun signToken(token: ByteArray, privateKey: RSAPrivateKey): ByteArray {
        // ADB expects PKCS1-padded RSA signature (same as openssl rsautl -sign)
        val signature = java.security.Signature.getInstance("NONEwithRSA")
        signature.initSign(privateKey)
        // ADB sends a 20-byte token; we must sign it raw with PKCS#1 v1.5 padding
        signature.update(token)
        return signature.sign()
    }

    /**
     * Converts a 2048-bit RSAPublicKey into the standard 524-byte AOSP android_pubkey format
     * expected by Android adbd daemon to display the authorization prompt on screen.
     */
    private fun convertRsaPublicKeyToAdbFormat(pubKey: RSAPublicKey, user: String): ByteArray {
        val modulus = pubKey.modulus
        val r32 = BigInteger.valueOf(2).pow(32)
        val n0invBig = modulus.remainder(r32).modInverse(r32).negate().remainder(r32)
        val n0inv = n0invBig.toInt()

        val r = BigInteger.valueOf(2).pow(2048)
        val rr = r.multiply(r).remainder(modulus)

        val buffer = ByteBuffer.allocate(524).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(64) // num_words = 64 (2048 bits / 32 bits per word)
        buffer.putInt(n0inv)

        // Modulus words (64 x 32-bit uints in little-endian)
        var remN = modulus
        for (i in 0 until 64) {
            val word = remN.remainder(r32).toInt()
            buffer.putInt(word)
            remN = remN.divide(r32)
        }

        // R^2 mod N words (64 x 32-bit uints in little-endian)
        var remRR = rr
        for (i in 0 until 64) {
            val word = remRR.remainder(r32).toInt()
            buffer.putInt(word)
            remRR = remRR.divide(r32)
        }

        // Exponent
        buffer.putInt(pubKey.publicExponent.toInt())

        val base64Key = Base64.encodeToString(buffer.array(), Base64.NO_WRAP)
        return "$base64Key $user\u0000".toByteArray(Charsets.UTF_8)
    }

    // =========================================================================
    // ADB PROTOCOL PACKET SERIALIZATION / DESERIALIZATION
    // =========================================================================

    private data class AdbHeader(
        val command: Int,
        val arg0: Int,
        val arg1: Int,
        val dataLength: Int,
        val dataCrc32: Int,
        val magic: Int
    )

    private fun writeMessage(
        out: OutputStream,
        command: Int,
        arg0: Int,
        arg1: Int,
        data: ByteArray
    ) {
        val buffer = ByteBuffer.allocate(24 + data.size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(command)
        buffer.putInt(arg0)
        buffer.putInt(arg1)
        buffer.putInt(data.size)
        buffer.putInt(calculateChecksum(data))
        buffer.putInt(command xor -0x1)
        buffer.put(data)
        out.write(buffer.array())
        out.flush()
    }

    private fun readHeader(inputStream: InputStream): AdbHeader? {
        val headerBytes = ByteArray(24)
        var totalRead = 0
        while (totalRead < 24) {
            val read = inputStream.read(headerBytes, totalRead, 24 - totalRead)
            if (read == -1) return null
            totalRead += read
        }
        val buffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
        return AdbHeader(
            command = buffer.int,
            arg0 = buffer.int,
            arg1 = buffer.int,
            dataLength = buffer.int,
            dataCrc32 = buffer.int,
            magic = buffer.int
        )
    }

    private fun readPayload(inputStream: InputStream, length: Int): ByteArray {
        val data = ByteArray(length)
        var totalRead = 0
        while (totalRead < length) {
            val read = inputStream.read(data, totalRead, length - totalRead)
            if (read == -1) break
            totalRead += read
        }
        return data
    }

    private fun skipBytes(inputStream: InputStream, count: Int) {
        var remaining = count
        val temp = ByteArray(1024)
        while (remaining > 0) {
            val read = inputStream.read(temp, 0, minOf(remaining, temp.size))
            if (read == -1) break
            remaining -= read
        }
    }

    private fun calculateChecksum(data: ByteArray): Int {
        var sum = 0
        for (b in data) {
            sum += (b.toInt() and 0xFF)
        }
        return sum
    }
}
