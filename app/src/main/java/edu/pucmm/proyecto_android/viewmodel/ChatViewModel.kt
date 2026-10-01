package edu.pucmm.proyecto_android.viewmodel

import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import edu.pucmm.proyecto_android.model.Mensaje
import edu.pucmm.proyecto_android.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel (private val otroUsuarioUid: String) : ViewModel() {

    private val repository = ChatRepository()
    private val chatId = repository.generarChatId(otroUsuarioUid)

    private val _mensajesReales = MutableStateFlow<List<Mensaje>>(emptyList())
    private val _pendientes = MutableStateFlow<List<Mensaje>>(emptyList())

    // lo que ve la pantalla: mensajes guardados + imagenes que todavia se estan subiendo
    val mensajes: StateFlow<List<Mensaje>> =
        combine(_mensajesReales, _pendientes) { reales, pendientes ->
            val idsReales = reales.map { it.id }.toSet()
            reales + pendientes.filter { it.id !in idsReales }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _errores = MutableSharedFlow<String>()
    val errores: SharedFlow<String> = _errores.asSharedFlow()

    val miUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var miNombre: String? = null

    init {
        escucharMensajes()
    }

    private fun escucharMensajes() {
        viewModelScope.launch {

            try {
                repository.escucharMensajes(chatId).collect { listaMensajes ->
                    android.util.Log.d("ChatDebug", "Mensajes recibidos: ${listaMensajes.size} para chatId=$chatId")
                    _mensajesReales.value = listaMensajes
                }

            } catch (e: Exception) {
                // Si falla solo no se actualiza la lista.
                android.util.Log.e("ChatDebug", "Error al escuchar mensajes: ${e.message}", e)
            }
        }
    }

    // muestra la imagen en el chat al instante, mientras se sube
    fun mostrarPendiente(uriLocal: String): String {
        val idMensaje = UUID.randomUUID().toString()
        val pendiente = Mensaje(
            id = idMensaje,
            idEmisor = miUid,
            imagenUrl = uriLocal,
            fecha = Timestamp.now()
        )
        _pendientes.value = _pendientes.value + pendiente
        return idMensaje
    }

    fun quitarPendiente(idMensaje: String) {
        _pendientes.value = _pendientes.value.filterNot { it.id == idMensaje }
    }

    fun enviarMensaje(texto: String) {
        val textoLimpio = texto.trim()
        if (textoLimpio.isEmpty())  return

        viewModelScope.launch {
            // primero lo busca en el firestore, y luego lo reutiliza
            val nombre =  miNombre ?: repository.obtenerMiNombre().also { miNombre = it }

            repository.enviarMensaje(chatId, textoLimpio, nombre)
                .onSuccess {
                    android.util.Log.d("ChatDebug", "Mensaje enviado correctamente a chatId=$chatId")
                }
                .onFailure { error ->
                    android.util.Log.e("ChatDebug", "Error al enviar mensaje: ${error.message}", error)
                }
        }
    }

    fun enviarImagen(bytes: ByteArray, idMensaje: String) {
        viewModelScope.launch {
            val nombre = miNombre ?: repository.obtenerMiNombre().also { miNombre = it }
            repository.enviarImagen(chatId, bytes, nombre, idMensaje)
                .onFailure {
                    android.util.Log.e("ChatDebug", "Error al enviar imagen: ${it.message}", it)
                    _errores.emit("No se pudo enviar la imagen")
                }
            quitarPendiente(idMensaje)
        }
    }

    // fabrica este viewModel pasandole el uid del otro usuario
    class Factory(private val otroUsuarioUid: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass :Class<T>) : T {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(otroUsuarioUid) as T
        }
    }
}