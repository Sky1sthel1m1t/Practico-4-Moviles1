package com.example.practico4.ui.activities

import android.content.Context
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Producto
import com.example.practico4.databinding.ActivityProductoDetailBinding
import com.example.practico4.models.ProductoApi
import com.example.practico4.repositories.ProductoRepository
import java.text.SimpleDateFormat

class ProductoDetailActivity : AppCompatActivity(), ProductoRepository.ProductoApiDetailListener,
    ProductoRepository.ProductoApiUpdateListener {
    private lateinit var binding: ActivityProductoDetailBinding
    private lateinit var db: AppDatabase
    private var idProducto: Int = -1
    private val formatter: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        idProducto = intent.getIntExtra("id", -1)

        if (idProducto != -1) {
            loadForm()
        }

        setupEventListeners()
    }

    private fun setupEventListeners() {
        binding.btnCancelar.setOnClickListener {
            finish()
        }
        binding.btnGuardar.setOnClickListener {
            saveProducto()
        }
    }

    private fun saveProducto() {
        val nombre = binding.txtNombre.editText?.text.toString()
        val precio = binding.txtPrecio.editText?.text.toString().toDouble()
        val descrpcion = binding.txtDescripcion.editText?.text.toString()
        val categoria_id = binding.txtCategoria.editText?.text.toString().toInt()

        if (!validarCategoriaExista(categoria_id)) {
            Toast.makeText(this, "La categoria no existe", Toast.LENGTH_SHORT).show()
            return
        }

        val producto = Producto(nombre, descrpcion, precio, categoria_id)

        if (idProducto == -1) {
            insertProducto(producto)
        } else {
            val aux = db.productoDao().getById(idProducto)
            producto.created_at = aux?.created_at.toString()
            producto.updated_at = aux?.updated_at.toString()
            producto.productoId = idProducto
            updateProducto(producto, true)
        }
        finish()
    }

    private fun loadForm() {
        val producto = db.productoDao().getById(idProducto)
        binding.txtNombre.editText?.setText(producto?.nombre)
        binding.txtDescripcion.editText?.setText(producto?.descripcion)
        binding.txtPrecio.editText?.setText(producto?.precio_actual.toString())
        binding.txtCategoria.editText?.setText(producto?.categoria_id.toString())
    }

    private fun validarCategoriaExista(categoria_id: Int): Boolean {
        return db.categoriaDao().getAllId().contains(categoria_id)
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

    private fun updateProducto(producto: Producto, isEdit: Boolean) {
        if (isOnline(this)) {
            ProductoRepository.updateProducto(producto, this, isEdit)
        } else {
            val fecha = Calendar.getInstance().time
            producto.updated_at = formatter.format(fecha).toString()
            db.productoDao().update(producto)
        }
    }

    override fun onProductoUpdateSuccess(producto: ProductoApi, toast: Boolean) {
        val productodb = Producto(
            producto.nombre,
            producto.descripcion,
            producto.precio_actual,
            producto.categoria.id
        )
        productodb.productoId = idProducto
        productodb.created_at = producto.created_at
        productodb.updated_at = producto.updated_at
        db.productoDao().update(productodb)
        if (toast) {
            Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onProductoUpdateError(error: Throwable) {
        Toast.makeText(this, "No se ha podido actualizar el producto", Toast.LENGTH_SHORT).show()
    }

    override fun onProductoInsertSuccess(producto: ProductoApi) {
        val productodb = Producto(
            producto.nombre,
            producto.descripcion,
            producto.precio_actual,
            producto.categoria.id
        )
        productodb.productoId = producto.id
        productodb.created_at = producto.created_at
        productodb.updated_at = producto.updated_at
        db.productoDao().insert(productodb)
        Toast.makeText(this, "Producto insertado", Toast.LENGTH_SHORT).show()
    }

    override fun onProductoInsertError(error: Throwable) {
        Toast.makeText(this, "No se ha podido insertar el producto", Toast.LENGTH_SHORT).show()
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