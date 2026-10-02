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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import edu.pucmm.proyecto_android.util.configurarBordes
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ConversacionesViewModel by viewModels()

    private val avatares = listOf("🦒", "🦊", "🐼", "🐯", "🐸", "🦄", "🐙", "🐧", "🦉", "🐨", "🐰", "🐻")

    private val pedirPermisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBordes(superior = binding.header, inferior = binding.rvUsuarios)
        setSupportActionBar(binding.toolbar)
        // para que no se mande con el titulo de la app, porque hay uno custom ya
        supportActionBar?.title = ""

        binding.rvUsuarios.layoutManager = LinearLayoutManager(this)

        binding.etBuscar.doOnTextChanged { texto, _, _, _ ->
            viewModel.buscar(texto?.toString().orEmpty())
        }

        observarEstado()
        solicitarPermisoNotificaciones()
        viewModel.miAvatar.observe(this) { avatar ->
            binding.tvMiAvatar.text = avatar.ifEmpty { "🙂" }
        }
        binding.tvMiAvatar.setOnClickListener { mostrarSelectorAvatar() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.accionCerrarSesion) {
            viewModel.cerrarSesion {
                irALogin()
            }
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
                        binding.tvVacio.text = if (viewModel.hayBusqueda()) {
                            "Sin resultados"
                        } else {
                            getString(R.string.no_hay_otros_usuarios_registrados)
                        }
                        binding.tvVacio.visibility = View.VISIBLE
                        binding.rvUsuarios.visibility = View.GONE
                    } else {
                        binding.tvVacio.visibility = View.GONE
                        binding.rvUsuarios.visibility = View.VISIBLE
                        binding.rvUsuarios.adapter = UsuarioAdapter(estado.usuarios) { usuario ->
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.putExtra("otroUsuarioUid", usuario.id)
                            intent.putExtra("otroUsuarioNombre", usuario.nombre)
                            intent.putExtra("otroUsuarioAvatar", usuario.avatar)
                            startActivity(intent)
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

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarSelectorAvatar() {
        val dp = resources.displayMetrics.density

        val cuadricula = GridLayout(this).apply {
            columnCount = 4
            setPadding((16 * dp).toInt(), (8 * dp).toInt(), (16 * dp).toInt(), 0)
        }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.elige_tu_avatar)
            .setView(cuadricula)
            .setNegativeButton("Cancelar", null)
            .create()

        avatares.forEach { emoji ->
            val celda = TextView(this).apply {
                text = emoji
                textSize = 32f
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams().apply {
                    width = (64 * dp).toInt()
                    height = (64 * dp).toInt()
                }
                setOnClickListener {
                    viewModel.elegirAvatar(emoji)
                    dialogo.dismiss()
                }
            }
            cuadricula.addView(celda)
        }

        dialogo.show()
    }
}