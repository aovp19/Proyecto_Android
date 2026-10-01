package edu.pucmm.proyecto_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.pucmm.proyecto_android.model.User
import edu.pucmm.proyecto_android.repository.AuthRepository
import edu.pucmm.proyecto_android.repository.UsuarioRepository
import kotlinx.coroutines.launch


class ConversacionesViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    private val _estado = MutableLiveData<UsuariosEstado>()
    val estado: LiveData<UsuariosEstado> = _estado

    // Carga los usuarios inmediatamente se carga el ViewModel
    init {
        cargarUsuarios()
        registrarTokenFcm()
    }

    fun cargarUsuarios() {
        _estado.value = UsuariosEstado.Cargando

        viewModelScope.launch {
            usuarioRepository.obtenerUsuarios()
                .onSuccess { lista -> _estado.value = UsuariosEstado.Exito(lista) }
                .onFailure { _estado.value = UsuariosEstado.Error("No se pudo cargar la lista de usuarios") }
        }
    }

    private fun registrarTokenFcm() {
        viewModelScope.launch { usuarioRepository.guardarTokenFcm() }
    }

    fun cerrarSesion(alTerminar: () -> Unit) {
        viewModelScope.launch {
            usuarioRepository.eliminarTokenFcm()
            authRepository.cerrarSesion()
            alTerminar()
        }
    }
}