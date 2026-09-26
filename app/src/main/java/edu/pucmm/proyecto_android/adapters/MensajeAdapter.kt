package edu.pucmm.proyecto_android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.pucmm.proyecto_android.databinding.ItemMensajeAjenoBinding
import edu.pucmm.proyecto_android.databinding.ItemMensajePropioBinding
import edu.pucmm.proyecto_android.model.Mensaje

class MensajeAdapter (

    private val mensajes: List<Mensaje>,
    private val miUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // modos de vistas
    companion object {
        private const val TIPO_PROPIO = 1
        private const val TIPO_AJENO = 2
    }

    // es inner pq no tiene nada que ver fuera de la clase MensajeAdapter
    inner class PropioViewHolder (val binding: ItemMensajePropioBinding) : RecyclerView.ViewHolder(binding.root)

    inner class AjenoViewHolder(val binding: ItemMensajeAjenoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TIPO_PROPIO) {
            PropioViewHolder (ItemMensajePropioBinding.inflate(inflater, parent, false))
        }
        else {
            AjenoViewHolder (ItemMensajeAjenoBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, posicion: Int) {
        val mensaje = mensajes[posicion]

        when (holder) {
            is PropioViewHolder -> holder.binding.tvTexto.text = mensaje.texto
            is AjenoViewHolder -> holder.binding.tvTexto.text = mensaje.texto
        }
    }

    override fun getItemCount(): Int = mensajes.size
}