package com.mc.lectornotificaciones

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        if (packageName != "com.bcp.innovacxion.yapeapp") return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return
        val text = extras.getString("android.text") ?: return

        if (title != "Confirmación de Pago") return

        val nombre = Regex("^(.+?) te envió").find(text)?.groupValues?.get(1)?.trim() ?: ""
        val monto = Regex("S/\\.\\s*([0-9]+(?:\\.[0-9]{2})?)").find(text)?.groupValues?.get(1) ?: ""
        val codigo = Regex("Cód\\. de seguridad es (\\d{3})").find(text)?.groupValues?.get(1) ?: ""

        Log.d("NotiParse", "Nombre: $nombre, Monto: $monto, Código: $codigo")

        val json = JSONObject().apply {
            put("nombre", nombre)
            put("monto", monto)
            put("codigo", codigo)
        }

        // 🔄 Enviar broadcast local para actualizar la UI
        val intent = Intent("com.mc.lectornotificaciones.PAGO_RECIBIDO").apply {
            putExtra("nombre", nombre)
            putExtra("monto", monto)
            putExtra("codigo", codigo)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        sendToBackend(json)
    }

    private fun sendToBackend(data: JSONObject) {
        Thread {
            try {
                val url = URL("http://TU_IP_O_DOMINIO:PUERTO/api/pagos") // 🛠️ Personaliza esto
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true
                conn.outputStream.write(data.toString().toByteArray(Charsets.UTF_8))
                conn.outputStream.flush()
                conn.outputStream.close()

                val responseCode = conn.responseCode
                Log.d("BackendResponse", "Código: $responseCode")
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("BackendError", e.toString())
            }
        }.start()
    }
}
