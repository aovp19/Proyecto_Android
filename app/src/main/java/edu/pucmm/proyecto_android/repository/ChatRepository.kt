package edu.pucmm.proyecto_android.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import edu.pucmm.proyecto_android.model.Mensaje
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    // genera el mismo id de chat sin importar el orden de los user ids
    fun generarChatId(otroUid: String): String {
        val miUid = auth.currentUser?.uid ?: ""
        return if (miUid < otroUid) "${miUid}_$otroUid" else "${otroUid}_$miUid"
    }

    // escucha los mensajes de un chat en tiempo real
    fun escucharMensajes(chatId: String): Flow<List<Mensaje>> = callbackFlow {
        val listener = firestore.collection("chats").document(chatId)
            .collection("mensajes")
            .orderBy("fecha")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val mensajes = snapshot?.documents?.mapNotNull { doc ->
                    // ignora documentos sin fecha valida
                    if (doc.getTimestamp("fecha") == null) return@mapNotNull null
                    doc.toObject(Mensaje::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(mensajes)
            }
        awaitClose { listener.remove() }
    }

    // trae el nombre del usuario que tiene la seccion activa
    suspend fun obtenerMiNombre(): String {
        val miUid = auth.currentUser?.uid ?: return ""

        return try {
            firestore.collection("usuarios").document(miUid)
                .get()
                .await()
                .getString("nombre") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // envia un mensaje de texto nuevo al chat
    suspend fun enviarMensaje(chatId: String, texto: String, nombreEmisor: String): Result<Unit> {
        return try {
            val miUid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No hay sesión activa"))

            val mensaje = hashMapOf(
                "idEmisor" to miUid,
                "nombreEmisor" to nombreEmisor,
                "texto" to texto,
                "fecha" to Timestamp.now()
            )

            firestore.collection("chats").document(chatId)
                .collection("mensajes")
                .add(mensaje)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // sube la imagen a Storage y envia el mensaje con su url (usa el id que decide la app)
    suspend fun enviarImagen(
        chatId: String,
        bytes: ByteArray,
        nombreEmisor: String,
        idMensaje: String
    ): Result<Unit> {
        return try {
            val miUid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No hay sesión activa"))

            val ref = storage.reference.child("chats/$chatId/${UUID.randomUUID()}.jpg")
            val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
            ref.putBytes(bytes, metadata).await()
            val url = ref.downloadUrl.await().toString()

            val mensaje = hashMapOf(
                "idEmisor" to miUid,
                "nombreEmisor" to nombreEmisor,
                "texto" to "",
                "imagenUrl" to url,
                "fecha" to Timestamp.now()
            )

            firestore.collection("chats").document(chatId)
                .collection("mensajes")
                .document(idMensaje)
                .set(mensaje)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}