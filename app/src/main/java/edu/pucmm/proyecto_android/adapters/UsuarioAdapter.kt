package edu.pucmm.proyecto_android.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.pucmm.proyecto_android.databinding.ItemUsuarioBinding
import edu.pucmm.proyecto_android.model.User

class UsuarioAdapter(
    private val usuarios: List<User>,
    private val onUsuarioClick: (User) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    // tonos morados para los avatares, cada usuario siempre recibe el mismo
    private val colores = intArrayOf(
        0xFF7E57C2.toInt(), 0xFF9575CD.toInt(), 0xFF5E35B1.toInt(),
        0xFFAB47BC.toInt(), 0xFF7986CB.toInt(), 0xFF8E24AA.toInt()
    )

    inner class UsuarioViewHolder(val binding: ItemUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]

        with(holder.binding) {
            tvNombreUsuario.text = usuario.nombre
            tvCorreo.text = usuario.email
            tvInicial.text = usuario.avatar.ifEmpty { usuario.nombre.trim().take(1).uppercase() }
            tvInicial.backgroundTintList = ColorStateList.valueOf(
                colores[Math.floorMod(usuario.id.hashCode(), colores.size)]
            )
            root.setOnClickListener { onUsuarioClick(usuario) }
        }
    }

    override fun getItemCount(): Int = usuarios.size
}