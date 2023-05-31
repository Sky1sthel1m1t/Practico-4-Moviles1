package com.example.practico4.dal.dto

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class VentaConDetalle(
    @Embedded val venta: Venta,
    @Relation(
        parentColumn = "ventaId",
        entityColumn = "productoId",
        associateBy = Junction(VentaProducto::class)
    )
    val productos: List<Producto>
) {
}