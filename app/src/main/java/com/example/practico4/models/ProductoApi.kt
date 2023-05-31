package com.example.practico4.models

data class ProductoApi(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio_actual: String,
    val created_at: String,
    val updated_at: String,
    val categoria: CategoriaApi
)