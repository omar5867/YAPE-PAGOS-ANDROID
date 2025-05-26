# 📲 Lector de Notificaciones de Yape – Proyecto Android

Esta aplicación Android permite **leer automáticamente las notificaciones de la app Yape** (BCP) y extraer datos como el nombre del remitente, el monto enviado y el código de confirmación. Luego, **envía esta información a un backend en Node.js/Express con MongoDB** para su almacenamiento.

## 🚀 Características

- Lectura automática de notificaciones de pagos de Yape.
- Extracción y validación mediante expresiones regulares.
- Almacenamiento local persistente (SharedPreferences).
- Envío automático de los datos al backend.
- Vista en tiempo real de los pagos dentro de la app.
- Consola de logs integrada para seguimiento desde el celular.
- Interfaz en pantalla completa.

## 🖼️ Captura de pantalla

_Agrega aquí una captura de pantalla si deseas._

## 🛠️ Requisitos

### Backend (Node.js)
- MongoDB corriendo localmente o remotamente.
- Express configurado para aceptar solicitudes desde la IP del celular (usa HTTPS o permite HTTP temporalmente).
- Ruta `/api/registrar-pagos-albarranicos` que reciba un JSON como:

```json
{
  "data": {
    "payName": "Juan Pérez",
    "payAmount": "15.00",
    "payCode": "123"
  }
}
