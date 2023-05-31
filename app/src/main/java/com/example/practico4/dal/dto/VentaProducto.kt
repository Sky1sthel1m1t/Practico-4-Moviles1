package com.example.practico4.dal.dto

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class VentaProducto(
    var ventaId : Int,
    var productoId : Int,
    var cantidad : Int,
    var precio : Double,
) {
    @PrimaryKey var ventaProductoId : Int? = null
}

//@Entity(primaryKeys = ["ventaId", "productoId"])
//data class VentaProducto(
//    var ventaId : Int,
//    var productoId : Int,
//    var cantidad : Int,
//    var precio : Double,
//) {
//    @PrimaryKey(autoGenerate = true)
//    var ventaProductoId : Int? = null
//}