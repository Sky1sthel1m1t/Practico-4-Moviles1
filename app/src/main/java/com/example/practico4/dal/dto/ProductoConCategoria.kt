package com.example.practico4.dal.dto

import androidx.room.Embedded
import androidx.room.Relation

data class ProductoConCategoria(
    @Embedded val producto: Producto,
    @Relation (
        parentColumn = "categoria_id",
        entityColumn = "id"
    )
    val categoria: Categoria
)