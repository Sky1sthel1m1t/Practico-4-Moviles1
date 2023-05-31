package com.example.practico4.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.practico4.R
import com.example.practico4.dal.dto.Categoria
import com.example.practico4.databinding.CategoriaListItemBinding

class CategoriaListAdapter(
    var categorias: List<Categoria>,
    val listener: CategoriaListListener
) : RecyclerView.Adapter<CategoriaListAdapter.CategoriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        val view = inflate.inflate(R.layout.categoria_list_item, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categorias.size
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.binding.lbIdCategoria.text = categoria.id.toString()
        holder.binding.lbNombreCategoria.text = categoria.nombre
        holder.itemView.setOnClickListener {
            listener.onCategoriaClick(categoria)
        }
        holder.binding.btnEditCategoria.setOnClickListener {
            listener.onCategoriaEditClick(categoria)
        }
        holder.binding.btnDeleteCategoria.setOnClickListener {
            listener.onCategoriaDeleteClick(categoria)
        }
    }

    fun reload(categorias: List<Categoria>) {
        this.categorias = categorias
        notifyDataSetChanged()
    }

    class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CategoriaListItemBinding.bind(itemView)
    }

    interface CategoriaListListener {
        fun onCategoriaEditClick(categoria: Categoria)
        fun onCategoriaDeleteClick(categoria: Categoria)
        fun onCategoriaClick(categoria: Categoria)
    }
}

