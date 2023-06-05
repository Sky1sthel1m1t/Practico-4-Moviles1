package com.example.practico4.ui.activities

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Categoria
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.ProductoConCategoria
import com.example.practico4.databinding.ActivityProductoListBinding
import com.example.practico4.models.CategoriaApi
import com.example.practico4.models.DeleteResponse
import com.example.practico4.models.DeletedModels
import com.example.practico4.models.ProductoApi
import com.example.practico4.repositories.CategoriaRepository
import com.example.practico4.repositories.ProductoRepository
import com.example.practico4.ui.adapters.ProductoListAdapter
import java.text.SimpleDateFormat

class ProductoListActivity : AppCompatActivity(), ProductoListAdapter.ProductoListListener,
    ProductoRepository.ProductoApiListListener, ProductoRepository.ProductoApiUpdateListener,
    ProductoRepository.ProductoApiDetailListener {
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
        if (isOnline(this)) {
            ProductoRepository.fetchListaProductos(this)
        }
        reloadList()
    }

    private fun reloadList() {
        binding.rvProductos.adapter.let { adapter ->
            if (adapter is ProductoListAdapter) {
                adapter.reload(db.productoDao().getProductoCategoria())
            }
        }
    }

    private fun eliminarProductosEliminados(lista: List<ProductoApi>) {
        val listaIds = lista.map { it.id }
        val ultimaFechaApi = formatter.parse(lista.last().created_at)
        DeletedModels.getInstance().deletedProductos.forEach {
            if (listaIds.contains(it.productoId)) {
                val fechaCategoria = formatter.parse(it.created_at)
                if (fechaCategoria.before(ultimaFechaApi) || fechaCategoria == ultimaFechaApi) {
                    ProductoRepository.deleteProducto(it.productoId!!, this, false)
                }
            }
        }
        DeletedModels.getInstance().deletedProductos.clear()
        reloadList()
    }

    private fun insertProductosPorInsertar(productos: ArrayList<Producto>) {
        productos.forEach {
            ProductoRepository.insertProducto(it, this)
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

    private fun verificarDeleteInsert(ids: ArrayList<Int>, ultimoProductoApi: ProductoApi) {
        val listProductos = db.productoDao().getProductoByNotIn(ids)
        val ultimaFecha = formatter.parse(ultimoProductoApi.created_at)
        listProductos.forEach {
            val fechaCategoria = formatter.parse(it.created_at)
            deleteProducto(it)
            if (fechaCategoria.after(ultimaFecha)) {
                insertProducto(it)
            }
        }
    }

    private fun insertProducto(producto: Producto) {
        if (isOnline(this)) {
            ProductoRepository.insertProducto(producto, this)
        } else {
            val fecha = Calendar.getInstance().time
            producto.created_at = formatter.format(fecha).toString()
            producto.updated_at = formatter.format(fecha).toString()
            db.productoDao().insert(producto)
        }
    }

    private fun deleteProducto(producto: Producto) {
        if (isOnline(this)) {
            producto.productoId?.let { ProductoRepository.deleteProducto(it, this, false) }
        } else {
            DeletedModels.getInstance().deletedProductos.add(producto)
            db.productoDao().delete(producto)
        }
    }

    override fun onProductoClick(producto: ProductoConCategoria) {
        val intent = Intent(this, ProductoDetailActivity::class.java)
        intent.putExtra("id", producto.producto.productoId)
        startActivity(intent)
    }

    override fun onProductoDeleteClick(producto: ProductoConCategoria) {
        producto.producto.productoId?.let {
            deleteProducto(producto.producto)
        }
        reloadList()
    }

    override fun onProductoListFetched(productos: List<ProductoApi>) {
        val productosPorInsertar = ArrayList<Producto>()
        val idsProductosApi = ArrayList<Int>()
        eliminarProductosEliminados(productos)
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
        verificarDeleteInsert(idsProductosApi, productos.last())
        insertProductosPorInsertar(productosPorInsertar)
        reloadList()
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

    override fun onProductoUpdateSuccess(productoApi: ProductoApi, toast: Boolean) {
        val producto = Producto(
            productoApi.nombre,
            productoApi.descripcion,
            productoApi.precio_actual,
            productoApi.categoria.id
        )
        producto.productoId = productoApi.id
        producto.created_at = productoApi.created_at
        producto.updated_at = productoApi.updated_at
        db.productoDao().update(producto)
        reloadList()
        if (toast) {
            Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onProductoUpdateError(error: Throwable) {
        Log.d("ProductoListActivity", "No se pudo actualizar el producto")
    }

    override fun onProductoInsertSuccess(producto: ProductoApi) {
        val productoDb = Producto(
            producto.nombre,
            producto.descripcion,
            producto.precio_actual,
            producto.categoria.id
        )
        productoDb.productoId = producto.id
        productoDb.created_at = producto.created_at
        productoDb.updated_at = producto.updated_at
        db.productoDao().insert(productoDb)
        reloadList()
    }

    override fun onProductoInsertError(error: Throwable) {
        Log.d("ProductoListActivity", "No se pudo insertar el producto ${error.printStackTrace()}")
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }
}