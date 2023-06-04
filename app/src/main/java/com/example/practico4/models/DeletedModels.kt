package com.example.practico4.models

import com.example.practico4.dal.dto.Categoria
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.Venta

class DeletedModels {
    companion object {
        private var instance: DeletedModels? = null
        var deletedCategorias = ArrayList<Categoria>()
        var deletedProductos = ArrayList<Producto>()
        var deletedVentas = ArrayList<Venta>()

        fun getInstance(): DeletedModels {
            if (instance == null) {
                instance = DeletedModels()
            }
            return instance!!
        }
    }
}