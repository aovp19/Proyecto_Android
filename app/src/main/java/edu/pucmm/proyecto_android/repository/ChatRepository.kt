package edu.pucmm.proyecto_android.repository

import android.system.Os.close
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.pucmm.proyecto_android.model.Mensaje
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import kotlinx.coroutines.tasks.await

class ChatRepository (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore =  FirebaseFirestore.getInstance()
) {

    // genera el mismo id de chat sin importar el orden de los user ids
    fun generarChatId(otroUid: String): String {
        val miUid = auth.currentUser?.uid ?: ""
        return if (miUid < otroUid) "${miUid}_$otroUid" else "${otroUid}_$miUid"
    }

    // envia un mensaje nuevo al chat
    suspend fun enviarMensaje(chatId: String, texto: String): Result<Unit> {
        return try {
            val miUid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No hay sesión activa"))


            val mensaje = hashMapOf(
                "idEmisor" to miUid,
                "texto" to texto,
                "fecha" to com.google.firebase.Timestamp.now()
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
                    doc.toObject(Mensaje::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(mensajes)
            }
        awaitClose { listener.remove() }
    }
}