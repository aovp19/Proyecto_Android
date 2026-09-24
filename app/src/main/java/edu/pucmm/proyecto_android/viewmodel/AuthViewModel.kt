package edu.pucmm.proyecto_android.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import edu.pucmm.proyecto_android.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Privado porque solo el ViewModel puede cambiar el valor
    private val _estado = MutableLiveData<AuthEstado>(AuthEstado.Inactivo)

    // Publico porque la pantalla lo observa, pero no modifica
    val estado: LiveData<AuthEstado> = _estado

    // Permite saber si mostrar el inicio de sesion o la app directamente
    fun haySesionActiva() : Boolean = repository.getUsuarioActual() != null

    // Cerrar sesion
    fun cerrarSesion() = repository.cerrarSesion()

    // Iniciar sesion
    fun iniciarSesion(email: String, password: String) {
        val correo = email.trim()

        val validacionErr = validarCorreoYPassword(correo, password)
        if(validacionErr != null) {
            _estado.value = AuthEstado.Error(validacionErr)
            return
        }

        _estado.value = AuthEstado.Cargando

        // lanzar una corrutina para esperar que firebase responda sin frizar la pantalla
        viewModelScope.launch{
            repository.iniciarSesion(correo, password)
                .onSuccess { _estado.value = AuthEstado.Exito }
                .onFailure { _estado.value = AuthEstado.Error(traducirError(it)) }
        }
    }

    fun registrar(nombre: String, email: String, password: String, confirmarPassword: String) {
        val nombreClean = nombre.trim()
        val correo = email.trim()

        // Validar el nombre
        if(nombreClean.isEmpty()) {
            _estado.value = AuthEstado.Error("Completa todos los campos")
            return
        }

        // Validar el correo y contraseña
        val validacionErr = validarCorreoYPassword(correo, password)
        if(validacionErr != null) _estado.value = AuthEstado.Error(validacionErr)

        //Validar que la contraseña tenga mas de 8 caracteres
        if(password.length < 8 ) {
            _estado.value = AuthEstado.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }

        //Validar si la confirmacion de la contraseña es igual a la contraseña
        if(password != confirmarPassword) {
            _estado.value = AuthEstado.Error("Las contraseñas no coinciden")
            return
        }

        _estado.value = AuthEstado.Cargando

        viewModelScope.launch {
            repository.registrar(correo, password, nombreClean)
                .onSuccess { _estado.value = AuthEstado.Exito }
                .onFailure { _estado.value = AuthEstado.Error(traducirError(it)) }
        }
    }

    // Validaciones

    private fun validarCorreoYPassword(correo: String, password: String) : String? {

        if(correo.isEmpty() || password.isEmpty()) return "Completa todos los campos"

        if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) return "Correo invalido"

        return null
    }

    // traduce los errores de ingles a español (los def de firebase estan en ingles)
    private fun traducirError(error: Throwable): String = when (error) {

        is FirebaseAuthWeakPasswordException  -> "La contraseña es muy debil, debe tener al menos 8 caracteres"
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseNetworkException -> "No hay conexion a internet"
        else -> "Error desconocido, intenta de nuevo"
    }
}