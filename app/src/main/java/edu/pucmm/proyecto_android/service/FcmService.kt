package edu.pucmm.proyecto_android.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.pucmm.proyecto_android.R
import edu.pucmm.proyecto_android.repository.UsuarioRepository
import edu.pucmm.proyecto_android.view.ChatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FcmService : FirebaseMessagingService() {

    companion object {
        private const val CANAL_MENSAJES = "mensajes"

        // uid del usuario con el que se tiene el chat abierto (no notificar en ese caso)
        @Volatile
        var chatAbiertoCon: String? = null
    }

    // Firebase genera un token nuevo: se guarda en el usuario
    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            UsuarioRepository().guardarTokenFcm(token)
        }
    }

    override fun onMessageReceived(mensaje: RemoteMessage) {
        val datos = mensaje.data
        val idEmisor = datos["idEmisor"] ?: return
        if (chatAbiertoCon == idEmisor) return

        mostrarNotificacion(
            idEmisor = idEmisor,
            nombre = datos["nombreEmisor"].orEmpty().ifEmpty { "Nuevo mensaje" },
            texto = datos["texto"].orEmpty()
        )
    }

    @SuppressLint("MissingPermission")
    private fun mostrarNotificacion(idEmisor: String, nombre: String, texto: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CANAL_MENSAJES, "Mensajes nuevos", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("otroUsuarioUid", idEmisor)
            putExtra("otroUsuarioNombre", nombre)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, idEmisor.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, CANAL_MENSAJES)
            .setSmallIcon(R.drawable.ic_enviar)
            .setContentTitle(nombre)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(idEmisor.hashCode(), notificacion)
    }
}