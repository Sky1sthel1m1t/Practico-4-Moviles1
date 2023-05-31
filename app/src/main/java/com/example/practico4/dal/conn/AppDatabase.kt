package com.example.practico4.dal.conn

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.practico4.dal.dao.CategoriaDao
import com.example.practico4.dal.dao.ProductoDao
import com.example.practico4.dal.dao.VentaDao
import com.example.practico4.dal.dto.Categoria
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.Venta
import com.example.practico4.dal.dto.VentaProducto

@Database(entities = [Producto::class, Categoria::class, Venta::class, VentaProducto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun ventaDao(): VentaDao

    companion object {
        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "practico4"
            ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
        }
    }
}