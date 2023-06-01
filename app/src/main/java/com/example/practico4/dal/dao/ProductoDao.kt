package com.example.practico4.dal.dao

import androidx.room.*
import com.example.practico4.dal.dto.Categoria
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.ProductoConCategoria

@Dao
interface ProductoDao {
    @Query("SELECT * FROM producto")
    fun getAll(): List<Producto>

    @Query("SELECT * FROM producto WHERE productoId = (:id)")
    fun getById(id: Int): Producto?

    @Query("SELECT * FROM producto WHERE productoId NOT IN (:ids)")
    fun getProductoByNotIn(ids: List<Int>): List<Producto>

    @Transaction
    @Query("SELECT * FROM producto")
    fun getProductoCategoria(): List<ProductoConCategoria>

    @Transaction
    @Query("SELECT * FROM producto WHERE productoId = (:id)")
    fun getProductoCategoriaById(id: Int): ProductoConCategoria?

    @Insert
    fun insert(producto: Producto) : Long

    @Update
    fun update(producto: Producto)

    @Delete
    fun delete(producto: Producto)
}