package edu.pucmm.proyecto_android.util

import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

// la app se dibuja de borde a borde: el header baja lo que mide la barra de estado,
// y la vista de abajo sube lo que mide la barra de navegacion o el teclado
fun ComponentActivity.configurarBordes(superior: View? = null, inferior: View? = null) {
    enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

    superior?.let { vista ->
        val base = vista.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(vista) { v, insets ->
            val tipos = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            v.updatePadding(top = base + insets.getInsets(tipos).top)
            insets
        }
    }

    inferior?.let { vista ->
        val base = vista.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(vista) { v, insets ->
            val tipos = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            v.updatePadding(bottom = base + insets.getInsets(tipos).bottom)
            insets
        }
    }
}