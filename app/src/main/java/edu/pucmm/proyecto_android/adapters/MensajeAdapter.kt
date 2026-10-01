package edu.pucmm.proyecto_android.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.pucmm.proyecto_android.databinding.ItemFechaBinding
import edu.pucmm.proyecto_android.databinding.ItemMensajeAjenoBinding
import edu.pucmm.proyecto_android.databinding.ItemMensajePropioBinding
import edu.pucmm.proyecto_android.model.Mensaje
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.load
import android.widget.ImageView
import androidx.core.view.isVisible

// lo que puede haber en la lista del chat, una etiqueta de dia o un mensaje
sealed class ItemChat {
    data class Etiqueta(val texto: String) : ItemChat()
    data class Burbuja(val mensaje: Mensaje) : ItemChat()
}

class MensajeAdapter(
    mensajes: List<Mensaje>,
    private val miUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // modos de vistas
    companion object {
        private const val TIPO_PROPIO = 1
        private const val TIPO_AJENO = 2
        private const val TIPO_FECHA = 3
    }

    // los formatos deben ser declarados antes de los items
    // se crean una sola vez y se reutilizan para todos los mensajes
    private val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val formatoClaveDia = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val formatoDiaLargo = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.getDefault())

    private val items: List<ItemChat> = construirItems(mensajes)

    // mezcla los mensajes con las etiquetas de dia
    private fun construirItems(mensajes: List<Mensaje>): List<ItemChat> {
        val resultado = mutableListOf<ItemChat>()
        var diaAnterior = ""

        for (mensaje in mensajes) {
            val fecha = mensaje.fecha.toDate()
            val dia = formatoClaveDia.format(fecha)

            // si cambio el dia respecto al mensaje anterior, va una etiqueta antes
            if (dia != diaAnterior) {
                resultado.add(ItemChat.Etiqueta(textoDelDia(fecha)))
                diaAnterior = dia
            }
            resultado.add(ItemChat.Burbuja(mensaje))
        }

        return resultado
    }

    private fun textoDelDia(fecha: Date): String {
        val milisegundos = fecha.time
        return when {
            DateUtils.isToday(milisegundos) -> "Hoy"
            DateUtils.isToday(milisegundos + DateUtils.DAY_IN_MILLIS) -> "Ayer"
            else -> formatoDiaLargo.format(fecha)
        }
    }

    // es inner pq no tiene nada que ver fuera de la clase MensajeAdapter
    inner class PropioViewHolder(val binding: ItemMensajePropioBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class AjenoViewHolder(val binding: ItemMensajeAjenoBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class FechaViewHolder(val binding: ItemFechaBinding) :
        RecyclerView.ViewHolder(binding.root)

    // le dice al recyclerview que layout usar para cada elemento de la lista
    override fun getItemViewType(posicion: Int): Int {
        return when (val item = items[posicion]) {
            is ItemChat.Etiqueta -> TIPO_FECHA
            is ItemChat.Burbuja ->
                if (item.mensaje.idEmisor == miUid) TIPO_PROPIO else TIPO_AJENO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TIPO_PROPIO -> PropioViewHolder(ItemMensajePropioBinding.inflate(inflater, parent, false))
            TIPO_AJENO -> AjenoViewHolder(ItemMensajeAjenoBinding.inflate(inflater, parent, false))
            else -> FechaViewHolder(ItemFechaBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, posicion: Int) {
        when (val item = items[posicion]) {
            is ItemChat.Etiqueta -> {
                (holder as FechaViewHolder).binding.tvEtiqueta.text = item.texto
            }
            is ItemChat.Burbuja -> {
                val hora = formatoHora.format(item.mensaje.fecha.toDate())

                when (holder) {
                    is PropioViewHolder -> {
                        mostrarImagen(holder.binding.ivImagen, item.mensaje.imagenUrl)
                        holder.binding.tvTexto.isVisible = item.mensaje.texto.isNotEmpty()
                        holder.binding.tvTexto.text = item.mensaje.texto
                        holder.binding.tvFecha.text = hora
                    }
                    is AjenoViewHolder -> {
                        mostrarImagen(holder.binding.ivImagen, item.mensaje.imagenUrl)
                        holder.binding.tvTexto.isVisible = item.mensaje.texto.isNotEmpty()
                        holder.binding.tvTexto.text = item.mensaje.texto
                        holder.binding.tvFecha.text = hora
                    }
                }
            }
        }
    }

    // ahora se cuentan los items (mensajes + etiquetas), no solo los mensajes
    override fun getItemCount(): Int = items.size

    private fun mostrarImagen(iv: ImageView, url: String) {
        iv.isVisible = url.isNotEmpty()
        if (url.isNotEmpty()) iv.load(url)
    }
}