package com.example.practico4.api

import com.example.practico4.dal.dto.Venta
import com.example.practico4.models.DeleteResponse
import com.example.practico4.models.VentaApi
import com.example.practico4.models.VentaApiInsert
import retrofit2.Call
import retrofit2.http.*

interface VentaService {
    @GET("ventas")
    fun getVentasList(): Call<List<VentaApi>>

    @POST("ventas")
    fun insertVenta(@Body venta: VentaApiInsert): Call<VentaApi>

    @PUT("ventas/{id}")
    fun editVenta(@Path("id") id: Int, @Body venta: Venta): Call<VentaApi>

    @DELETE("ventas/{id}")
    fun deleteVenta(@Path("id") id: Int): Call<DeleteResponse>
}