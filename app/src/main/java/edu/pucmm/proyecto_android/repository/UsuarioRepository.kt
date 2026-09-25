package edu.pucmm.proyecto_android.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.pucmm.proyecto_android.model.User
import kotlinx.coroutines.tasks.await

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
}