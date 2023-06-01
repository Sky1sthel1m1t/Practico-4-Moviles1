package com.example.practico4.ui.activities

import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.ProductoConCategoria
import com.example.practico4.databinding.ActivityProductoListBinding
import com.example.practico4.models.DeleteResponse
import com.example.practico4.models.ProductoApi
import com.example.practico4.repositories.ProductoRepository
import com.example.practico4.ui.adapters.ProductoListAdapter
import java.text.SimpleDateFormat

class ProductoListActivity : AppCompatActivity(), ProductoListAdapter.ProductoListListener,
    ProductoRepository.ProductoApiListListener, ProductoRepository.ProductoApiUpdateListener {
    private lateinit var binding: ActivityProductoListBinding
    private lateinit var db: AppDatabase
    private val formatter: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoListBinding.inflate(layoutInflater)
        db = AppDatabase.getInstance(this)
        setContentView(binding.root)

        setupEventListeners()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val productos = db.productoDao().getProductoCategoria()
        binding.rvProductos.apply {
            adapter = ProductoListAdapter(productos, this@ProductoListActivity)
            layoutManager = LinearLayoutManager(this@ProductoListActivity).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        }
    }

    private fun setupEventListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, ProductoDetailActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        ProductoRepository.fetchListaProductos(this)
        reloadList()
    }

    private fun reloadList() {
        binding.rvProductos.adapter.let { adapter ->
            if (adapter is ProductoListAdapter) {
                adapter.reload(db.productoDao().getProductoCategoria())
            }
        }
    }

    override fun onProductoClick(producto: ProductoConCategoria) {
        val intent = Intent(this, ProductoDetailActivity::class.java)
        intent.putExtra("id", producto.producto.productoId)
        startActivity(intent)
    }

    override fun onProductoDeleteClick(producto: ProductoConCategoria) {
        producto.producto.productoId?.let {
            ProductoRepository.deleteProducto(it, this, true)
        }
        reloadList()
    }

    override fun onProductoListFetched(productos: List<ProductoApi>) {
        val productosPorInsertar = ArrayList<Producto>()
        val idsProductosApi = ArrayList<Int>()
        for (productoApi in productos) {
            val productoDb = Producto(
                productoApi.nombre,
                productoApi.descripcion,
                productoApi.precio_actual,
                productoApi.categoria.id
            )
            productoDb.productoId = productoApi.id
            productoDb.created_at = productoApi.created_at
            productoDb.updated_at = productoApi.updated_at
            idsProductosApi.add(productoApi.id)
            try {
                db.productoDao().insert(productoDb)
            } catch (e: Exception) {
                val productoPorInsertar = db.productoDao().getById(productoApi.id)

                if (productoDb.created_at == productoPorInsertar?.created_at
                    && productoDb.updated_at == productoPorInsertar.updated_at
                ) {
                    continue
                }

                if (productoDb.updated_at != productoPorInsertar?.updated_at
                    && productoDb.created_at == productoPorInsertar?.created_at
                ) {
                    verificarActualizacion(productoApi, productoPorInsertar)
                    continue
                }

                productoPorInsertar?.let { productosPorInsertar.add(it) }
                db.productoDao().update(productoDb)
            }
        }
        verificarDelete(idsProductosApi)
        insertProductosPorInsertar(productosPorInsertar)
        reloadList()
    }

    private fun insertProductosPorInsertar(productos: ArrayList<Producto>) {
        productos.forEach {
            val fecha = formatter.format(Calendar.getInstance().time).toString()
            it.created_at = fecha
            it.updated_at = fecha
            db.productoDao().insert(it)
        }
    }

    private fun verificarActualizacion(productoApi: ProductoApi, productoDb: Producto) {
        val fechaApi = formatter.parse(productoApi.updated_at)
        val fechaDB = formatter.parse(productoDb.updated_at)
        if (fechaApi.after(fechaDB)) {
            productoDb.nombre = productoApi.nombre
            productoDb.updated_at = productoApi.updated_at
            db.productoDao().update(productoDb)
        } else {
            ProductoRepository.updateProducto(productoDb, this, false)
        }
    }

    private fun verificarDelete(ids: ArrayList<Int>) {
        val listProductos = db.productoDao().getProductoByNotIn(ids)
        listProductos.forEach {
            db.productoDao().delete(it)
        }
    }

    override fun onProductoListFetchError(error: Throwable) {
        Toast.makeText(this, "Hubo un error sal y vuelve a entrar", Toast.LENGTH_SHORT).show()
    }

    override fun onProductoDeleteSuccess(id: Int, deleteResponse: DeleteResponse, toast: Boolean) {
        if (toast) {
            val producto = db.productoDao().getById(id)
            producto?.let {
                db.productoDao().delete(it)
                Toast.makeText(this, deleteResponse.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onProductoDeleteError(error: Throwable) {
        Toast.makeText(this, "No se pudo borrar el producto", Toast.LENGTH_SHORT).show()
    }

    override fun onProductoUpdateSuccess(producto: ProductoApi, toast: Boolean) {
        Log.d("ProductoListActivity", "Producto actualizado")
    }

    override fun onProductoUpdateError(error: Throwable) {
        Log.d("ProductoListActivity", "No se pudo actualizar el producto")
    }
}