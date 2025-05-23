package com.mc.lectornotificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mc.lectornotificaciones.ui.theme.LectorNotificacionesTheme
import org.json.JSONArray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pantalla completa sin barra de estado ni navegación
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        setContent {
            LectorNotificacionesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }

    @Composable
    fun AppContent() {
        val context = LocalContext.current
        val prefs = context.getSharedPreferences("pagos", Context.MODE_PRIVATE)
        val listaJson = JSONArray(prefs.getString("lista", "[]"))
        val pagos = remember { mutableStateListOf<Pago>() }

        for (i in 0 until listaJson.length()) {
            pagos.add(Pago.fromJson(listaJson.getJSONObject(i)))
        }

        // Receptor de nuevos pagos
        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val nombre = intent?.getStringExtra("nombre") ?: return
                    val monto = intent.getStringExtra("monto") ?: return
                    val codigo = intent.getStringExtra("codigo") ?: return

                    val nuevoPago = Pago(nombre, monto, codigo)
                    pagos.add(nuevoPago)
                }
            }
            val filter = IntentFilter("com.mc.lectornotificaciones.NEW_PAGO")
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            onDispose {
                context.unregisterReceiver(receiver)
            }
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            Button(
                onClick = {
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Habilitar acceso a notificaciones")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pagos recibidos:", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(pagos) { pago ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nombre: ${pago.nombre}")
                            Text("Monto: S/ ${pago.monto}")
                            Text("Código: ${pago.codigo}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("JSON enviado:")
                            Text(
                                text = pago.toJson().toString(2),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
