package edu.pucmm.proyecto_android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.pucmm.proyecto_android.databinding.ItemUsuarioBinding
import edu.pucmm.proyecto_android.model.User


class UsuarioAdapter(
    private val usuarios: List<User>,
    private val onUsuarioClick: (User) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

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
        holder.binding.tvNombreUsuario.text = usuario.nombre

        holder.binding.root.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    override fun getItemCount(): Int = usuarios.size
}