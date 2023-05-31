package com.example.practico4.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.practico4.R
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.ProductoConCategoria
import com.example.practico4.databinding.ProductoListItemBinding

class ProductoListAdapter(
    private var productos: List<ProductoConCategoria>,
    val listener: ProductoListListener
) : RecyclerView.Adapter<ProductoListAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.producto_list_item, parent, false)
        return ProductoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productos.size
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.binding.lbId.text = producto.producto.productoId.toString()
        holder.binding.lbNombre.text = producto.producto.nombre
        holder.binding.lbPrecio.text = producto.producto.precio_actual.toString()
        holder.binding.lbCategoriaIdProducto.text = producto.producto.categoria_id.toString()
        holder.binding.lbNombreCategoriaProducto.text = producto.categoria.nombre
        holder.itemView.setOnClickListener {
            listener.onProductoClick(producto)
        }
        holder.binding.btnDelete.setOnClickListener {
            listener.onProductoDeleteClick(producto)
        }
    }

    fun reload(productos : List<ProductoConCategoria>) {
        this.productos = productos
        notifyDataSetChanged()
    }

    class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProductoListItemBinding.bind(itemView)
    }

    interface ProductoListListener {
        fun onProductoClick(producto: ProductoConCategoria)
        fun onProductoDeleteClick(producto: ProductoConCategoria)
    }
}
