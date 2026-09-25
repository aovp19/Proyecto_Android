package edu.pucmm.proyecto_android.viewmodel

import edu.pucmm.proyecto_android.model.User

sealed class UsuariosEstado {
    object Cargando : UsuariosEstado() // no ha sucedido nada, procesando...
    data class Exito(val usuarios: List<User>) : UsuariosEstado()  // operación exitosa, trae la lista de usuarios
    data class Error(val mensaje: String) : UsuariosEstado() // error y su mensaje
}