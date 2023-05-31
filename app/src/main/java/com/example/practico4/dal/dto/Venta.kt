package com.example.practico4.dal.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Venta(
    var nombre: String,
    var nit: Long,
    var usuario: String
    ) {
    @PrimaryKey var ventaId: Int? = null
    var created_at: String = ""
    var updated_at: String = ""
}