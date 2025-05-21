package com.mc.lectornotificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mc.lectornotificaciones.ui.theme.LectorNotificacionesTheme

class MainActivity : ComponentActivity() {

    private val pagos = mutableStateListOf<Pago>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Abrir ajustes de notificaciones
        val intentSettings = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intentSettings)

        // Escuchar pagos recibidos
        val filter = IntentFilter("com.mc.lectornotificaciones.PAGO_RECIBIDO")
        LocalBroadcastManager.getInstance(this).registerReceiver(pagoReceiver, filter)

        setContent {
            LectorNotificacionesTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PagosList(pagos)
                }
            }
        }
    }

    private val pagoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val nombre = intent?.getStringExtra("nombre") ?: return
            val monto = intent.getStringExtra("monto") ?: return
            val codigo = intent.getStringExtra("codigo") ?: return

            val pago = Pago(nombre, monto, codigo)
            pagos.add(0, pago) // Agregar al inicio
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pagoReceiver)
        super.onDestroy()
    }
}

data class Pago(val nombre: String, val monto: String, val codigo: String)

@Composable
fun PagosList(pagos: List<Pago>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pagos recibidos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(pagos) { pago ->
                PagoItem(pago)
                Divider()
            }
        }
    }
}

@Composable
fun PagoItem(pago: Pago) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Nombre: ${pago.nombre}")
        Text("Monto: S/. ${pago.monto}")
        Text("Código: ${pago.codigo}")
    }
}
