package com.example.practico4.api


import com.example.practico4.dal.dto.Categoria
import com.example.practico4.models.CategoriaApi
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoriaService {
    @GET("categorias")
    fun getCategoriasList(): Call<List<CategoriaApi>>

    @POST("categorias")
    fun insertCategoria(): Call<Categoria>

    @PUT("categorias/{id}")
    fun editCategoria(@Path("id") int: Int): Call<Categoria>

    @DELETE("categorias/{id}")
    fun deleteCategoria(): Call<Categoria>
}