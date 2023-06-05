package com.example.practico4.models

data class VentaApi(
    val id: Int,
    val nombre: String,
    val nit: String,
    val usuario: String,
    val created_at: String,
    val updated_at: String,
    val detalle: List<DetalleApi>
)