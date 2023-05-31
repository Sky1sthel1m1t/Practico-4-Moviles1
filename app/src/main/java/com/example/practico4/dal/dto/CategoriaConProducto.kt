package com.example.practico4.dal.dto

import androidx.room.Embedded
import androidx.room.Relation

data class CategoriaConProducto(
    @Embedded val categoria: Categoria,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoria_id"
    )
    val productos: List<Producto>
)