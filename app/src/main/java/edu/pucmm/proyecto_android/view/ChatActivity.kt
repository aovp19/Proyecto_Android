package edu.pucmm.proyecto_android.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.pucmm.proyecto_android.adapters.MensajeAdapter
import edu.pucmm.proyecto_android.databinding.ActivityChatBinding
import edu.pucmm.proyecto_android.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        val otroUsuarioUid = intent.getStringExtra("otroUsuarioUid") ?: ""
        val otroUsuarioNombre = intent.getStringExtra("otroUsuarioNombre") ?: ""

        binding.tvNombreUsuario.text = otroUsuarioNombre

        viewModel = ViewModelProvider(this, ChatViewModel.Factory(otroUsuarioUid))[ChatViewModel::class.java]

        binding.rvMensajes.layoutManager = LinearLayoutManager(this)

        binding.btnEnviar.setOnClickListener {
            val texto = binding.etMensaje.text.toString()
            viewModel.enviarMensaje(texto)
            binding.etMensaje.setText("")
        }

        observarMensajes()

        // para hacer scroll al final de la pantalla cuando salga el teclado
        binding.etMensaje.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.rvMensajes.postDelayed({
                    val itemCount = binding.rvMensajes.adapter?.itemCount ?: 0
                    if (itemCount > 0) {
                        binding.rvMensajes.scrollToPosition(itemCount - 1)
                    }
                }, 300)
            }
        }
    }

    private fun observarMensajes() {
        lifecycleScope.launch {
            viewModel.mensajes.collect { listaMensajes ->
                binding.rvMensajes.adapter = MensajeAdapter(listaMensajes, viewModel.miUid)
                if (listaMensajes.isNotEmpty()) {
                    binding.rvMensajes.scrollToPosition(listaMensajes.size - 1)
                }
            }
        }
    }
}