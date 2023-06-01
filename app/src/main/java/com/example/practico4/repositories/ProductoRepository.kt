package com.example.practico4.repositories

import com.example.practico4.api.ProductoService
import com.example.practico4.dal.dto.Producto
import com.example.practico4.models.DeleteResponse
import com.example.practico4.models.ProductoApi
import com.example.practico4.ui.activities.ProductoListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ProductoRepository {
    private val retrofit = RetrofitRepository.getRetrofitInstance()
    private val productoService = retrofit.create(ProductoService::class.java)

    fun fetchListaProductos(listener: ProductoApiListListener) {
        productoService.getProductosList().enqueue(object : Callback<List<ProductoApi>> {
            override fun onResponse(
                call: Call<List<ProductoApi>>,
                response: Response<List<ProductoApi>>
            ) {
                if (response.isSuccessful) {
                    val productos = response.body()
                    productos?.let {
                        listener.onProductoListFetched(it)
                    }
                }
            }

            override fun onFailure(call: Call<List<ProductoApi>>, t: Throwable) {
                listener.onProductoListFetchError(t)
            }
        })
    }

    fun updateProducto(producto: Producto, listener: ProductoApiUpdateListener, toast: Boolean) {
        producto.productoId?.let {
            productoService.editProducto(it, producto)
                .enqueue(object : Callback<ProductoApi> {
                    override fun onResponse(
                        call: Call<ProductoApi>,
                        response: Response<ProductoApi>
                    ) {
                        if (response.isSuccessful) {
                            val producto = response.body()
                            producto?.let {
                                listener.onProductoUpdateSuccess(it, toast)
                            }
                        }
                    }

                    override fun onFailure(call: Call<ProductoApi>, t: Throwable) {
                        listener.onProductoUpdateError(t)
                    }
                })
        }
    }

    fun insertProducto(producto: Producto, listener: ProductoApiDetailListener) {
        productoService.insertProducto(producto).enqueue(object : Callback<ProductoApi> {
            override fun onResponse(call: Call<ProductoApi>, response: Response<ProductoApi>) {
                if (response.isSuccessful) {
                    val producto = response.body()
                    producto?.let {
                        listener.onProductoInsertSuccess(it)
                    }
                }
            }

            override fun onFailure(call: Call<ProductoApi>, t: Throwable) {
                listener.onProductoInsertError(t)
            }
        })
    }

    fun deleteProducto(id: Int, listener: ProductoApiListListener, toast: Boolean) {
        productoService.deleteProducto(id)
            .enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(
                    call: Call<DeleteResponse>,
                    response: Response<DeleteResponse>
                ) {
                    if (response.isSuccessful) {
                        val deleteResponse = response.body()
                        deleteResponse?.let {
                            listener.onProductoDeleteSuccess(id, it, toast)
                        }
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    listener.onProductoDeleteError(t)
                }
            })
    }

    interface ProductoApiListListener {
        fun onProductoListFetched(productos: List<ProductoApi>)
        fun onProductoListFetchError(error: Throwable)
        fun onProductoDeleteSuccess(id: Int, deleteResponse: DeleteResponse, toast: Boolean)
        fun onProductoDeleteError(error: Throwable)
    }

    interface ProductoApiDetailListener {
        fun onProductoInsertSuccess(producto: ProductoApi)
        fun onProductoInsertError(error: Throwable)
    }

    interface ProductoApiUpdateListener {
        fun onProductoUpdateSuccess(producto: ProductoApi, toast: Boolean)
        fun onProductoUpdateError(error: Throwable)
    }
}