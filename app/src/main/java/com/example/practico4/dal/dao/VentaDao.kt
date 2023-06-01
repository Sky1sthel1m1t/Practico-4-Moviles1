package com.example.practico4.dal.dao

import androidx.room.*
import com.example.practico4.dal.dto.Venta
import com.example.practico4.dal.dto.VentaConDetalle
import com.example.practico4.dal.dto.VentaProducto

@Dao
interface VentaDao {
    @Query("SELECT * FROM venta")
    fun getAll(): List<Venta>

    @Query("SELECT * FROM venta WHERE ventaId = :id")
    fun getById(id: Int): Venta?

    @Query("SELECT * FROM venta")
    fun getAllWithProductos(): VentaConDetalle

    @Query("SELECT * FROM ventaproducto WHERE ventaId = :ventaId")
    fun getVentaProductos(ventaId: Int): List<VentaProducto>

    @Query("SELECT * FROM ventaproducto")
    fun getAllVentaProductos(): List<VentaProducto>

    @Query("DELETE FROM ventaproducto WHERE ventaId = :ventaId")
    fun deleteProductosEnVenta(ventaId: Int) : Int

    @Query("DELETE FROM ventaproducto WHERE ventaId = :ventaId AND productoId = :productoId")
    fun deleteProductoEnVenta(ventaId: Int, productoId: Int) : Int

    @Query("SELECT * FROM venta WHERE ventaId NOT IN (:ids)")
    fun getVentaByNotIn(ids: List<Int>): List<Venta>

    @Insert
    fun insertProductoVendido(ventaProductos: VentaProducto)

    @Update
    fun updateProductoVendido(ventaProductos: VentaProducto)

    @Insert
    fun deleteProductoVendido(ventaProductos: VentaProducto)

    @Insert
    fun insert(venta: Venta): Long

    @Update
    fun update(venta: Venta)

    @Delete
    fun delete(venta: Venta)


}