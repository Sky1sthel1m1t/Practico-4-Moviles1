package com.example.practico4.dal.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Categoria(var nombre : String) {
    @PrimaryKey var id : Int? = null
    var created_at : String = ""
    var updated_at : String = ""
}
