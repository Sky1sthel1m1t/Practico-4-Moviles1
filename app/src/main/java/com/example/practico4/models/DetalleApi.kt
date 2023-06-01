package com.example.practico4.models

data class DetalleApi(
    val cantidad: Int,
    val precio: String,
    val producto: ProductoApi
)