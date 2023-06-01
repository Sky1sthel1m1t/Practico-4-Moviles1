package com.example.practico4.models

data class VentaApiInsert(
    val nombre: String,
    val nit: String,
    val usuario: String,
    val productos: List<DetalleInsert>
)