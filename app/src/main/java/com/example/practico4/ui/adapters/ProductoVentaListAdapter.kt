package com.example.practico4.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.practico4.R
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.databinding.ProductoventaListItemBinding
import com.example.practico4.models.DetalleInsert

class ProductoVentaListAdapter(
    var detalle: List<DetalleInsert>,
    val db: AppDatabase
) :
    RecyclerView.Adapter<ProductoVentaListAdapter.ProductoVentaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoVentaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.productoventa_list_item, parent, false)
        return ProductoVentaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return detalle.size
    }

    override fun onBindViewHolder(holder: ProductoVentaViewHolder, position: Int) {
        val detalle = this.detalle[position]
        val producto = db.productoDao().getById(detalle.id)

        holder.binding.lbCantidadProductoVenta.text = detalle.cantidad.toString()
        holder.binding.lbPrecioProductoVenta.text = detalle.precio.toString()
        holder.binding.lbNombreProductoVenta.text = producto?.nombre
    }

    fun reload(detalles : List<DetalleInsert>) {
        this.detalle = detalles
        notifyDataSetChanged()
    }

    class ProductoVentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProductoventaListItemBinding.bind(itemView)
    }

}
