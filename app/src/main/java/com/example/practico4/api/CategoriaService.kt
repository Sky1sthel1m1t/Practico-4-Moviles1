package com.example.practico4.api


import com.example.practico4.dal.dto.Categoria
import com.example.practico4.models.CategoriaApi
import com.example.practico4.models.DeleteResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoriaService {
    @GET("categorias")
    fun getCategoriasList(): Call<List<CategoriaApi>>

    @POST("categorias")
    fun insertCategoria(@Body categoria: Categoria): Call<CategoriaApi>

    @PUT("categorias/{id}")
    fun editCategoria(@Path("id") int: Int, @Body categoria: Categoria): Call<CategoriaApi>

    @DELETE("categorias/{id}")
    fun deleteCategoria(@Path("id") int: Int): Call<DeleteResponse>
}