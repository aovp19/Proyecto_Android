package edu.pucmm.proyecto_android.repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import edu.pucmm.proyecto_android.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Retorna el usuario actual si ya hay una sesión iniciada, de lo contrario, retorna null
    fun getUsuarioActual (): FirebaseUser? {
        return auth.currentUser
    }

    // Registra un usuario nuevo con correo y contraseña
    suspend fun registrar(email: String, password: String, nombre: String): Result<Unit> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(email, password).await()
            val id = resultado.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario")

            val nuevoUsuario = User(id = id, nombre = nombre, email = email)
            firestore.collection("usuarios").document(id).set(nuevoUsuario).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Inicia sesion con email y contraseña
    suspend fun iniciarSesion(email: String, password: String) : Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cierra la sesion actual
    fun cerrarSesion() {
        auth.signOut()
    }

}

