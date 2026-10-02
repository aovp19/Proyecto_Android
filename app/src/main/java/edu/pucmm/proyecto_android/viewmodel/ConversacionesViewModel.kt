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

    private val _miAvatar = MutableLiveData("")
    val miAvatar: LiveData<String> = _miAvatar

    val estado: LiveData<UsuariosEstado> = _estado

    private var todosLosUsuarios: List<User> = emptyList()

    private var textoBusqueda = ""

    // Carga los usuarios inmediatamente se carga el ViewModel
    init {
        cargarUsuarios()
        registrarTokenFcm()
        cargarMiAvatar()
    }

    fun cargarUsuarios() {
        _estado.value = UsuariosEstado.Cargando

        viewModelScope.launch {
            usuarioRepository.obtenerUsuarios()
                .onSuccess { lista ->
                    todosLosUsuarios = lista
                    publicarFiltrados()
                }
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

    fun buscar(texto: String) {
        textoBusqueda = texto.trim()
        publicarFiltrados()
    }

    fun hayBusqueda(): Boolean = textoBusqueda.isNotEmpty()

    private fun publicarFiltrados() {
        val filtrados = if (textoBusqueda.isEmpty()) {
            todosLosUsuarios
        } else {
            todosLosUsuarios.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
        }
        _estado.value = UsuariosEstado.Exito(filtrados)
    }

    private fun cargarMiAvatar() {
        viewModelScope.launch { _miAvatar.value = usuarioRepository.obtenerMiAvatar() }
    }

    fun elegirAvatar(avatar: String) {
        _miAvatar.value = avatar // se ve al instante
        viewModelScope.launch { usuarioRepository.guardarAvatar(avatar) }
    }
}