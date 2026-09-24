package edu.pucmm.proyecto_android.viewmodel

sealed class AuthEstado {

    object Inactivo : AuthEstado()          // No ha sucedido nada
    object Cargando : AuthEstado()          // Esperando respuesta de firebase
    object Exito : AuthEstado()             // el regritro o loguin salio bien o fallo
    data class Error(val mensaje: String) : AuthEstado()    // fallo y trae el mensaje
}