package edu.pucmm.proyecto_android.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.pucmm.proyecto_android.R
import edu.pucmm.proyecto_android.adapters.UsuarioAdapter
import edu.pucmm.proyecto_android.databinding.ActivityMainBinding
import edu.pucmm.proyecto_android.viewmodel.ConversacionesViewModel
import edu.pucmm.proyecto_android.viewmodel.UsuariosEstado

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ConversacionesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // para que no se mande con el titulo de la app, porque hay uno custom ya
        supportActionBar?.title = ""

        binding.rvUsuarios.layoutManager = LinearLayoutManager(this)

        observarEstado()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.accionCerrarSesion) {
            viewModel.cerrarSesion()
            irALogin()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observarEstado() {
        viewModel.estado.observe(this) { estado ->
            when (estado) {
                is UsuariosEstado.Cargando -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvUsuarios.visibility = View.GONE
                    binding.tvVacio.visibility = View.GONE
                }
                is UsuariosEstado.Exito -> {
                    binding.progressBar.visibility = View.GONE

                    if (estado.usuarios.isEmpty()) {
                        binding.tvVacio.visibility = View.VISIBLE
                        binding.rvUsuarios.visibility = View.GONE
                    } else {
                        binding.tvVacio.visibility = View.GONE
                        binding.rvUsuarios.visibility = View.VISIBLE
                        binding.rvUsuarios.adapter = UsuarioAdapter(estado.usuarios) { usuario ->
                            // Aqui se abre el chat con este usuario en el paso 5
                        }
                    }
                }
                is UsuariosEstado.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvVacio.visibility = View.VISIBLE
                    binding.tvVacio.text = estado.mensaje
                }
            }
        }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}