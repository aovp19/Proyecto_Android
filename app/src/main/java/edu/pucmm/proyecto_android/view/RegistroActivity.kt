package edu.pucmm.proyecto_android.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.pucmm.proyecto_android.MainActivity
import edu.pucmm.proyecto_android.databinding.ActivityRegistroBinding
import edu.pucmm.proyecto_android.viewmodel.AuthEstado
import edu.pucmm.proyecto_android.viewmodel.AuthViewModel

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotones()
        observarEstado()
    }

    private fun configurarBotones() {
        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmarPassword = binding.etConfirmarPassword.text.toString()
            viewModel.registrar(nombre, email, password, confirmarPassword)
        }

        binding.tvLogin.setOnClickListener {
            finish() // regresa a LoginActivity, que sigue abierta detras
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
        binding.btnRegistrar.isEnabled = !cargando
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