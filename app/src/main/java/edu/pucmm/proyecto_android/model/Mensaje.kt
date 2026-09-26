package edu.pucmm.proyecto_android.model

import com.google.firebase.Timestamp

data class Mensaje (
    val id: String = "",
    val idEmisor: String = "",
    val texto: String = "",
    val fecha: Timestamp = Timestamp.now()
)