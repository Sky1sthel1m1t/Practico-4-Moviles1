package com.example.practico4.dal.dao

import androidx.room.*
import com.example.practico4.dal.dto.Categoria
import com.example.practico4.dal.dto.CategoriaConProducto

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categoria")
    fun getAll(): List<Categoria>

    @Query("SELECT * FROM categoria WHERE id = (:id)")
    fun getById(id: Int): Categoria?

    @Query("SELECT id FROM categoria")
    fun getAllId(): List<Int>

    @Query("SELECT * FROM categoria WHERE id NOT IN (:ids)")
    fun getCategoriaByNotIn(ids: List<Int>): List<Categoria>

    @Transaction
    @Query("SELECT * FROM categoria WHERE id = (:id)")
    fun getCategoriaProducto(id: Int): CategoriaConProducto

    @Insert
    fun insert(categoria: Categoria) : Long

    @Update
    fun update(categoria: Categoria)

    @Delete
    fun delete(categoria: Categoria)
}