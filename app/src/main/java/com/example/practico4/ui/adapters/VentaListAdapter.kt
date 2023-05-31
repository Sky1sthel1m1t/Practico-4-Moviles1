package com.example.practico4.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.practico4.R
import com.example.practico4.dal.dto.ProductoConCategoria
import com.example.practico4.dal.dto.Venta
import com.example.practico4.databinding.VentaListItemBinding

class VentaListAdapter(
    var ventas: List<Venta>,
    val listener: VentaListListener
) : RecyclerView.Adapter<VentaListAdapter.VentaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.venta_list_item, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = ventas[position]
        holder.binding.lbIdVenta.text = venta.ventaId.toString()
        holder.binding.lbNombreVenta.text = venta.nombre
        holder.binding.lbNitVenta.text = venta.nit.toString()
        holder.binding.lbUsuarioVenta.text = venta.usuario
        holder.itemView.setOnClickListener {
            listener.onVentaClick(venta)
        }
        holder.binding.btnDeleteVenta.setOnClickListener {
            listener.onVentaDeleteClick(venta)
        }
    }

    override fun getItemCount(): Int {
        return ventas.size
    }

    fun reload(ventas: List<Venta>) {
        this.ventas = ventas
        notifyDataSetChanged()
    }

    class VentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = VentaListItemBinding.bind(itemView)
    }

    interface VentaListListener {
        fun onVentaClick(venta: Venta)
        fun onVentaDeleteClick(venta: Venta)
    }
}
