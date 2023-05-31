package com.example.practico4.dal.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Producto(
    var nombre: String,
    var descripcion: String,
    var precio_actual: Double,
    var categoria_id: Int,
) {
    @PrimaryKey
    var productoId: Int? = null
    var created_at: String = ""
    var updated_at: String = ""

    override fun toString(): String {
        return "$nombre $precio_actual Bs"
    }
}
