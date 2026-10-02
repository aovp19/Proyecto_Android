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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import edu.pucmm.proyecto_android.util.ImagenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.app.NotificationManagerCompat
import edu.pucmm.proyecto_android.service.FcmService
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import edu.pucmm.proyecto_android.util.configurarBordes

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel

    private var uidOtro = ""

    private val selectorImagen = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        val idMensaje = viewModel.mostrarPendiente(uri.toString())
        lifecycleScope.launch {
            val bytes = withContext(Dispatchers.IO) { ImagenUtil.comprimir(contentResolver, uri) }
            if (bytes != null) viewModel.enviarImagen(bytes, idMensaje)
            else viewModel.quitarPendiente(idMensaje)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBordes(superior = binding.header, inferior = binding.barraMensaje)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        val otroUsuarioUid = intent.getStringExtra("otroUsuarioUid") ?: ""
        uidOtro = otroUsuarioUid
        val otroUsuarioNombre = intent.getStringExtra("otroUsuarioNombre") ?: ""

        binding.tvNombreUsuario.text = otroUsuarioNombre
        binding.btnVolver.setOnClickListener { finish() }
        val avatar = intent.getStringExtra("otroUsuarioAvatar").orEmpty()
        binding.tvAvatarChat.text = avatar.ifEmpty { otroUsuarioNombre.trim().take(1).uppercase() }

        viewModel = ViewModelProvider(this, ChatViewModel.Factory(otroUsuarioUid))[ChatViewModel::class.java]

        binding.rvMensajes.layoutManager = LinearLayoutManager(this)

        binding.btnEnviar.setOnClickListener {
            val texto = binding.etMensaje.text.toString()
            viewModel.enviarMensaje(texto)
            binding.etMensaje.setText("")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errores.collect {
                    Toast.makeText(this@ChatActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnAdjuntar.setOnClickListener {
            selectorImagen.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
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
                val adapter = MensajeAdapter(listaMensajes, viewModel.miUid)
                binding.rvMensajes.adapter = adapter

                // usamos el total de items del adapter, no de mensajes
                if (adapter.itemCount > 0) {
                    binding.rvMensajes.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FcmService.chatAbiertoCon = uidOtro
        NotificationManagerCompat.from(this).cancel(uidOtro.hashCode())
    }

    override fun onStop() {
        FcmService.chatAbiertoCon = null
        super.onStop()
    }
}