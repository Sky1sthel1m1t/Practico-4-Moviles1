package com.example.practico4.repositories

import com.example.practico4.api.VentaService
import com.example.practico4.dal.dto.Venta
import com.example.practico4.models.VentaApi
import com.example.practico4.models.VentaApiInsert
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object VentaRepository {
    val retrofit = RetrofitRepository.getRetrofitInstance()
    val ventaService = retrofit.create(VentaService::class.java)

    fun fetchListaVentas(listener: VentaApiListListener) {
        ventaService.getVentasList().enqueue(object : Callback<List<VentaApi>> {
            override fun onResponse(
                call: Call<List<VentaApi>>,
                response: Response<List<VentaApi>>
            ) {
                if (response.isSuccessful) {
                    val ventas = response.body()
                    ventas?.let {
                        listener.onVentaListFetched(it)
                    }
                }
            }

            override fun onFailure(call: Call<List<VentaApi>>, t: Throwable) {
                listener.onVentaListFetchError(t)
            }
        })
    }

    fun insertVenta(venta: VentaApiInsert, listener: VentaApiInsertListener) {
        ventaService.insertVenta(venta).enqueue(object : Callback<VentaApi> {
            override fun onResponse(
                call: Call<VentaApi>,
                response: Response<VentaApi>
            ) {
                print("")
                if (response.isSuccessful) {
                    val venta = response.body()
                    venta?.let {
                        listener.onVentaInsert(it)
                    }
                }
            }

            override fun onFailure(call: Call<VentaApi>, t: Throwable) {
                listener.onVentaInsertError(t)
            }
        })
    }

    fun updateVenta(venta: Venta, listener: VentaApiUpdateListener, toast: Boolean) {
        venta.ventaId?.let {
            ventaService.editVenta(it, venta)
                .enqueue(object : Callback<VentaApi> {
                    override fun onResponse(
                        call: Call<VentaApi>,
                        response: Response<VentaApi>
                    ) {
                        if (response.isSuccessful) {
                            val venta = response.body()
                            venta?.let {
                                listener.onVentaUpdateSuccess(it, toast)
                            }
                        }
                    }

                    override fun onFailure(call: Call<VentaApi>, t: Throwable) {
                        listener.onVentaUpdateError(t)
                    }
                })
        }
    }

    interface VentaApiListListener {
        fun onVentaListFetched(ventas: List<VentaApi>)
        fun onVentaListFetchError(error: Throwable)
    }

    interface VentaApiInsertListener {
        fun onVentaInsert(venta: VentaApi)
        fun onVentaInsertError(error: Throwable)
    }

    interface VentaApiUpdateListener {
        fun onVentaUpdateSuccess(venta: VentaApi, toast: Boolean)
        fun onVentaUpdateError(error: Throwable)
    }


}