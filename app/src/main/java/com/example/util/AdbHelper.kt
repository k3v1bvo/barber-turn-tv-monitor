package com.example.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Lightweight Pure Kotlin ADB Client:
 * Connects directly to Android TV (localhost 127.0.0.1 or remote IP over Wi-Fi) on port 5555
 * and executes "appops set <package> SYSTEM_ALERT_WINDOW allow" without needing external apps.
 */
object AdbHelper {

    private const val TAG = "AdbHelper"
    private const val A_CNXN = 0x4e584e43
    private const val A_AUTH = 0x48545541
    private const val A_OPEN = 0x4e45504f
    private const val A_OKAY = 0x59414b4f
    private const val A_CLSE = 0x45534c43
    private const val A_WRTE = 0x45545257

    private const val A_VERSION = 0x01000000
    private const val MAX_PAYLOAD = 4096

    sealed class AdbResult {
        data class Success(val message: String) : AdbResult()
        data class NeedsAuth(val message: String) : AdbResult()
        data class Error(val message: String) : AdbResult()
    }

    suspend fun grantOverlayPermission(
        targetIp: String = "127.0.0.1",
        targetPort: Int = 5555,
        targetPackage: String = "com.aistudio.barberturnostv.kxmpzq"
    ): AdbResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            socket = Socket()
            val cleanIp = targetIp.trim().ifBlank { "127.0.0.1" }
            socket.connect(InetSocketAddress(cleanIp, targetPort), 4000)
            socket.soTimeout = 5000

            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            // 1. Send CNXN Packet
            val cnxnPayload = "host::BarberSiteTV\u0000".toByteArray(Charsets.UTF_8)
            writeMessage(outputStream, A_CNXN, A_VERSION, MAX_PAYLOAD, cnxnPayload)

            // 2. Read Response from TV
            val header = readHeader(inputStream) ?: return@withContext AdbResult.Error("No se recibió respuesta del dispositivo.")

            when (header.command) {
                A_CNXN -> {
                    // Connected & Authenticated! Skip payload
                    if (header.dataLength > 0) {
                        skipBytes(inputStream, header.dataLength)
                    }
                }
                A_AUTH -> {
                    // Device requires authentication confirmation on TV screen
                    if (header.dataLength > 0) {
                        skipBytes(inputStream, header.dataLength)
                    }
                    return@withContext AdbResult.NeedsAuth(
                        "⚠️ Mira la pantalla de la TV y pulsa 'Aceptar' en el mensaje de depuración USB que acaba de aparecer, luego vuelve a presionar este botón."
                    )
                }
                else -> {
                    return@withContext AdbResult.Error("Respuesta ADB inesperada: 0x${Integer.toHexString(header.command)}")
                }
            }

            // 3. Send OPEN command with shell execution
            val localId = 1
            val commandString = "shell:appops set $targetPackage SYSTEM_ALERT_WINDOW allow\u0000"
            val commandPayload = commandString.toByteArray(Charsets.UTF_8)
            writeMessage(outputStream, A_OPEN, localId, 0, commandPayload)

            // 4. Read Response (OKAY or CLSE)
            val openResponse = readHeader(inputStream)
            if (openResponse != null && (openResponse.command == A_OKAY || openResponse.command == A_WRTE)) {
                return@withContext AdbResult.Success("¡Permiso de superposición concedido con éxito en la TV!")
            }

            return@withContext AdbResult.Success("Comando enviado a la TV.")
        } catch (e: java.net.ConnectException) {
            return@withContext AdbResult.Error("No se pudo conectar a $targetIp:$targetPort. Asegúrate de que 'Opciones de desarrollador' y 'Depuración USB/Red' estén activadas en la TV.")
        } catch (e: java.net.SocketTimeoutException) {
            return@withContext AdbResult.Error("Tiempo de espera agotado al conectar a $targetIp. Verifica que la TV y el celular estén en el mismo Wi-Fi.")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing ADB command", e)
            return@withContext AdbResult.Error("Error: ${e.localizedMessage ?: e.message}")
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

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
