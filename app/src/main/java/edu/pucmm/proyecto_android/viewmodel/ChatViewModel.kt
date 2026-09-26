package edu.pucmm.proyecto_android.viewmodel

import androidx.lifecycle.MutableLiveData
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

    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    val miUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        escucharMensajes()
    }

    private fun escucharMensajes() {
        viewModelScope.launch {

            try {
                repository.escucharMensajes(chatId).collect { listaMensajes ->
                    _mensajes.value = listaMensajes
                }

            } catch (e: Exception) {
                // Si falla solo no se actualiza la lista.
                // TODO: manejar el error
                e.printStackTrace()
            }
        }
    }

    fun enviarMensaje(texto: String) {
        val textoLimpio = texto.trim()
        if (textoLimpio.isEmpty())  return

        viewModelScope.launch {
            repository.enviarMensaje(chatId, textoLimpio)
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