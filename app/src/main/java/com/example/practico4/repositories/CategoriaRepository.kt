package com.example.practico4.repositories

import com.example.practico4.api.CategoriaService
import com.example.practico4.models.CategoriaApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object CategoriaRepository {
    private val retrofit = RetrofitRepository.getRetrofitInstance()
    private val categoriaService = retrofit.create(CategoriaService::class.java)

    fun fetchListaCategorias(listener: CategoriaListListener) {
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

    // Ver como pasar los datos como formato raw json

    fun updateCategoria(categoriaApi: CategoriaApi, listener: CategoriaUpdateListener) {
        categoriaService.editCategoria(categoriaApi.id, categoriaApi).enqueue(object : Callback<CategoriaApi> {
            override fun onResponse(call: Call<CategoriaApi>, response: Response<CategoriaApi>) {
                if (response.isSuccessful) {
                    val categoria = response.body()
                    categoria?.let {
                        listener.onCategoriaUpdateSuccess(it)
                    }
                }
            }

            override fun onFailure(call: Call<CategoriaApi>, t: Throwable) {
                listener.onCategoriaUpdateError(t)
            }
        })
    }


    interface CategoriaListListener {
        fun onCategoriaListFetched(list: List<CategoriaApi>)
        fun onCategoriaListFetchError(error: Throwable)
    }

    interface CategoriaUpdateListener {
        fun onCategoriaUpdateSuccess(categoriaApi: CategoriaApi)
        fun onCategoriaUpdateError(error: Throwable)
    }
}