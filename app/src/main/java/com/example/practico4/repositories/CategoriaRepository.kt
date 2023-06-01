package com.example.practico4.repositories

import com.example.practico4.api.CategoriaService
import com.example.practico4.dal.dto.Categoria
import com.example.practico4.models.CategoriaApi
import com.example.practico4.models.DeleteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CategoriaRepository {
    private val retrofit = RetrofitRepository.getRetrofitInstance()
    private val categoriaService = retrofit.create(CategoriaService::class.java)

    fun fetchListaCategorias(listener: CategoriaApiListener) {
        categoriaService.getCategoriasList().enqueue(object : Callback<List<CategoriaApi>> {
            override fun onResponse(
                call: Call<List<CategoriaApi>>,
                response: Response<List<CategoriaApi>>
            ) {
                if (response.isSuccessful) {
                    val categorias = response.body()
                    categorias?.let {
                        listener.onCategoriaListFetched(it)
                    }
                }
            }

            override fun onFailure(call: Call<List<CategoriaApi>>, t: Throwable) {
                listener.onCategoriaListFetchError(t)
            }
        })
    }

    fun updateCategoria(categoria: Categoria, listener: CategoriaApiListener, toast: Boolean) {
        categoria.id?.let {
            categoriaService.editCategoria(it, categoria)
                .enqueue(object : Callback<CategoriaApi> {
                    override fun onResponse(
                        call: Call<CategoriaApi>,
                        response: Response<CategoriaApi>
                    ) {
                        if (response.isSuccessful) {
                            val categoria = response.body()
                            categoria?.let {
                                listener.onCategoriaUpdateSuccess(it, toast)
                            }
                        }
                    }

                    override fun onFailure(call: Call<CategoriaApi>, t: Throwable) {
                        listener.onCategoriaUpdateError(t)
                    }
                })
        }
    }

    fun insertCategoria(categoria: Categoria, listener: CategoriaApiListener) {
        categoriaService.insertCategoria(categoria).enqueue(object : Callback<CategoriaApi> {
            override fun onResponse(call: Call<CategoriaApi>, response: Response<CategoriaApi>) {
                if (response.isSuccessful) {
                    val categoria = response.body()
                    categoria?.let {
                        listener.onCategoriaInsertSuccess(it)
                    }
                }
            }

            override fun onFailure(call: Call<CategoriaApi>, t: Throwable) {
                listener.onCategoriaInsertError(t)
            }
        })
    }

    fun deleteCategoria(id: Int, listener: CategoriaApiListener) {
        categoriaService.deleteCategoria(id).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(
                call: Call<DeleteResponse>,
                response: Response<DeleteResponse>
            ) {
                if (response.isSuccessful) {
                    val respuesta = response.body()
                    respuesta?.let {
                        listener.onCategoriaDeleteSuccess(it)
                    }
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                listener.onCategoriaDeleteError(t)
            }
        })
    }

    interface CategoriaApiListener {
        fun onCategoriaListFetched(list: List<CategoriaApi>)
        fun onCategoriaListFetchError(error: Throwable)
        fun onCategoriaUpdateSuccess(categoriaApi: CategoriaApi, toast : Boolean)
        fun onCategoriaUpdateError(error: Throwable)
        fun onCategoriaInsertSuccess(categoriaApi: CategoriaApi)
        fun onCategoriaInsertError(error: Throwable)
        fun onCategoriaDeleteSuccess(respuesta: DeleteResponse)
        fun onCategoriaDeleteError(error: Throwable)
    }
}