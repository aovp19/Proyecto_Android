package edu.pucmm.proyecto_android.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import edu.pucmm.proyecto_android.model.User
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class UsuarioRepository (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ) {

    private val usuariosCollection = firestore.collection("usuarios")

    //Trae todos los usuarios registrados menos el que tiene la sesion activa
    suspend fun obtenerUsuarios(): Result <List<User>> {
        return try {
            val uidActual = auth.currentUser?.uid
            val documentos = usuariosCollection.get().await()
            val listaUsuarios = documentos.toObjects(User::class.java)
                .filter { it.id != uidActual }

            Result.success(listaUsuarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enviarImagen(chatId: String, uri: Uri, nombreEmisor: String): Result<Unit> {
        return try {
            val miUid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No hay sesión activa"))

            val ref = FirebaseStorage.getInstance().reference
                .child("chats/$chatId/${UUID.randomUUID()}.jpg")

            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()

            val mensaje = hashMapOf(
                "idEmisor" to miUid,
                "nombreEmisor" to nombreEmisor,
                "texto" to "",
                "imagenBase64" to url,
                "fecha" to Timestamp.now()
            )
            firestore.collection("chats").document(chatId)
                .collection("mensajes").add(mensaje).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // guarda el token FCM de este dispositivo en el documento del usuario
    suspend fun guardarTokenFcm(token: String? = null): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No hay sesión activa"))
            val tokenActual = token ?: FirebaseMessaging.getInstance().token.await()

            usuariosCollection.document(uid)
                .set(mapOf("fcmTokens" to FieldValue.arrayUnion(tokenActual)), SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // al cerrar sesion, este dispositivo deja de recibir notificaciones de esa cuenta
    suspend fun eliminarTokenFcm() {
        try {
            val uid = auth.currentUser?.uid ?: return
            withTimeoutOrNull(3000.milliseconds) {
                val token = FirebaseMessaging.getInstance().token.await()
                usuariosCollection.document(uid)
                    .update("fcmTokens", FieldValue.arrayRemove(token)).await()
            }
        } catch (e: Exception) {
            // si falla, se limpia despues cuando la Cloud Function detecte el token invalido
        }
    }
}