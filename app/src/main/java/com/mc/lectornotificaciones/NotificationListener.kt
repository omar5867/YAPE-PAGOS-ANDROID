package com.mc.lectornotificaciones

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.bcp.innovacxion.yapeapp") return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return
        val text = extras.getString("android.text") ?: return

        Log.d("NotiTitle", title)
        Log.d("NotiText", text)

        if (title != "Confirmación de Pago") return

        // Regex para nombre con puntos, acentos, etc.
        val regex = Regex(
            """^([\p{L}\s.]+?) te envió un pago por S/ ?(\d+(?:\.\d{1,2})?)\.? .*?c[oó]d\. de seguridad es: ?(\d{3})""",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(text)

        if (match != null) {
            val nombre = match.groupValues[1].trim()
            val montoBruto = match.groupValues[2].trim()
            val monto = montoBruto.trimEnd('.')
            val codigo = match.groupValues[3].trim()


            val pago = Pago(nombre, monto, codigo)

            // Guardar en SharedPreferences
            val prefs = getSharedPreferences("pagos", MODE_PRIVATE)
            val listaActual = prefs.getString("lista", "[]")
            val listaJson = JSONArray(listaActual)
            listaJson.put(pago.toJson())
            prefs.edit().putString("lista", listaJson.toString()).apply()

            // Broadcast para actualizar UI
            val intent = Intent("com.mc.lectornotificaciones.NEW_PAGO")
            intent.putExtra("nombre", pago.nombre)
            intent.putExtra("monto", pago.monto)
            intent.putExtra("codigo", pago.codigo)
            sendBroadcast(intent)

            // Enviar al backend
            sendToBackend(pago)

            Log.d("NotifParsed", "Nombre: $nombre, Monto: $monto, Código: $codigo")
        } else {
            Log.d("NotifFail", "No se pudo extraer información de: $text")
        }
    }



    private fun sendToBackend(pago: Pago) {
        val json = pago.toJson()
        Thread {
            try {
                val url = URL("http://192.168.1.106:1337/api/registrar-pagos-albarranicos") // Cambiar por IP del backend
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true
                conn.outputStream.write(json.toString().toByteArray(Charsets.UTF_8))
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
