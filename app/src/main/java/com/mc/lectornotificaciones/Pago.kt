package com.mc.lectornotificaciones

import org.json.JSONObject

data class Pago(val nombre: String, val monto: String, val codigo: String) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("data", JSONObject().apply {
            put("payName", nombre)
            put("payAmount", monto)
            put("payCode", codigo)
        })
    }

    companion object {
        fun fromJson(obj: JSONObject) = Pago(
            obj.getString("nombre"),
            obj.getString("monto"),
            obj.getString("codigo")
        )
    }
}
