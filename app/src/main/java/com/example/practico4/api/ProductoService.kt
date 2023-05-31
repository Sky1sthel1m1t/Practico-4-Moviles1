package com.example.practico4.api

import com.example.practico4.dal.dto.Producto
import com.example.practico4.models.ProductoApi
import retrofit2.Call
import retrofit2.http.*

interface ProductoService {
    @GET("productos")
    fun getProductosList(): Call<List<ProductoApi>>

    @POST("productos")
    fun insertProducto(): Call<Producto>

    @PUT("productos/{id}")
    fun editProducto(@Path("id") id: Int): Call<Producto>

    @DELETE("productos/{id}")
    fun deleteProducto(@Path("id") id: Int): Call<Producto>
}