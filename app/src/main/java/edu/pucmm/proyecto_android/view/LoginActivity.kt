package edu.pucmm.proyecto_android.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.pucmm.proyecto_android.databinding.ActivityLoginBinding
import edu.pucmm.proyecto_android.viewmodel.AuthEstado
import edu.pucmm.proyecto_android.viewmodel.AuthViewModel
import edu.pucmm.proyecto_android.util.configurarBordes

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBordes(superior = binding.header, inferior = binding.root)

        if (viewModel.haySesionActiva()) {
            irAlInicio()
            return
        }

        configurarBotones()
        observarEstado()
    }

    private fun configurarBotones() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.iniciarSesion(email, password)
        }

        binding.tvRegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observarEstado() {
        viewModel.estado.observe(this) { estado ->
            when (estado) {
                is AuthEstado.Inactivo -> {
                    mostrarCargando(false)
                    mostrarError(null)
                }
                is AuthEstado.Cargando -> {
                    mostrarCargando(true)
                    mostrarError(null)
                }
                is AuthEstado.Exito -> {
                    mostrarCargando(false)
                    irAlInicio()
                }
                is AuthEstado.Error -> {
                    mostrarCargando(false)
                    mostrarError(estado.mensaje)
                }
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !cargando
    }

    private fun mostrarError(mensaje: String?) {
        binding.tvError.text = mensaje
        binding.tvError.visibility = if (mensaje != null) View.VISIBLE else View.GONE
    }

    private fun irAlInicio() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}