package com.example.practico4.ui.activities

import android.content.Context
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.Venta
import com.example.practico4.dal.dto.VentaProducto
import com.example.practico4.databinding.ActivityVentaDetailBinding
import com.example.practico4.models.DetalleApi
import com.example.practico4.models.DetalleInsert
import com.example.practico4.models.VentaApi
import com.example.practico4.models.VentaApiInsert
import com.example.practico4.repositories.ProductoRepository
import com.example.practico4.repositories.VentaRepository
import com.example.practico4.ui.adapters.ProductoVentaListAdapter
import java.text.SimpleDateFormat

class VentaDetailActivity : AppCompatActivity(), VentaRepository.VentaApiUpdateListener, VentaRepository.VentaApiInsertListener {
    private lateinit var binding: ActivityVentaDetailBinding
    private lateinit var db: AppDatabase
    private var idVenta: Int = -1
    private var ventaProducto = ArrayList<DetalleInsert>()
    private val formatter: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaDetailBinding.inflate(layoutInflater)
        db = AppDatabase.getInstance(this)
        setContentView(binding.root)

        idVenta = intent.getIntExtra("idVenta", -1)

        if (idVenta != -1) {
            loadForm()
            binding.btnAgregarProducto.visibility = View.INVISIBLE
        }

        setupEventListeners()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        reloadList()
    }

    private fun setupRecyclerView() {
        var lista = ArrayList<DetalleInsert>()
        binding.rvProductosVenta.apply {
            adapter = ProductoVentaListAdapter(lista, db)
            layoutManager = LinearLayoutManager(this@VentaDetailActivity)
        }
    }

    private fun reloadList(){
        binding.rvProductosVenta.adapter?.let {
            (it as ProductoVentaListAdapter).reload(ventaProducto)
        }
    }

    private fun setupEventListeners() {
        binding.btnCancelVenta.setOnClickListener {
            finish()
        }
        binding.btnGuardarVenta.setOnClickListener {
            saveVenta()
        }
        binding.btnAgregarProducto.setOnClickListener {
            addProducto()
        }
    }

    private fun saveVenta() {
        val nombre = binding.txtNombreVenta.editText?.text.toString()
        val nit = binding.txtNitVenta.editText?.text.toString()
        val usuario = binding.txtUsuarioVenta.editText?.text.toString()

        if (!validarForm(nombre, nit, usuario)) {
            return
        }

        val venta = Venta(
            nombre,
            nit.toLong(),
            usuario
        )

        if (idVenta != -1) {
            val aux = db.ventaDao().getById(idVenta)
            venta.created_at = aux?.created_at.toString()
            venta.updated_at = aux?.updated_at.toString()
            venta.ventaId = idVenta
            updateVenta(venta)
        } else {
            val ventaApiInsert = VentaApiInsert(
                nombre,
                nit,
                usuario,
                ventaProducto
            )
            insertVenta(ventaApiInsert)
        }
        finish()
    }

    private fun insertVenta(venta: VentaApiInsert) {
        if (isOnline(this)) {
            VentaRepository.insertVenta(venta, this)
        } else {
            val fecha = Calendar.getInstance().time
            val ventadb = Venta(
                venta.nombre,
                venta.nit.toLong(),
                venta.usuario
            )
            ventadb.created_at = formatter.format(fecha).toString()
            ventadb.updated_at = formatter.format(fecha).toString()
            val ventaId = db.ventaDao().insert(ventadb)
            insertVentaProducto(venta.productos, ventaId)
        }
    }

    private fun insertVentaProducto(productos: List<DetalleInsert>, ventaId: Long) {
        productos.forEach {
            val ventaProducto = VentaProducto(
                ventaId.toInt(),
                it.id,
                it.cantidad,
                it.precio
            )
            db.ventaDao().insertProductoVendido(ventaProducto)
        }
    }

    private fun updateVenta(venta: Venta) {
        if (isOnline(this)) {
            VentaRepository.updateVenta(venta, this, true)
        } else {
            val fecha = Calendar.getInstance().time
            venta.updated_at = formatter.format(fecha).toString()
            db.ventaDao().update(venta)
        }
    }

    private fun addProducto() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ingrese el id del producto que desea añadir a la venta")
        val layout = LinearLayout(this)
        val txtProductoId = EditText(this)
        val txtCantidad = EditText(this)
        val txtPrecio = EditText(this)
        layout.orientation = LinearLayout.VERTICAL
        txtProductoId.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
        txtCantidad.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
        txtPrecio.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        txtProductoId.hint = "Id del producto"
        txtCantidad.hint = "Cantidad"
        txtPrecio.hint = "Precio"
        layout.addView(txtProductoId)
        layout.addView(txtCantidad)
        layout.addView(txtPrecio)
        builder.setView(layout)
        builder.setPositiveButton("Añadir") { dialog, _ ->

            if (!validarAgregarProducto(
                    txtProductoId.text.toString(),
                    txtCantidad.text.toString(),
                    txtPrecio.text.toString()
                )
            ) {
                dialog.dismiss()
                return@setPositiveButton
            }

            val idProducto = txtProductoId.text.toString().toInt()
            val cantidad = txtCantidad.text.toString().toInt()
            val precio = txtPrecio.text.toString().toDouble()

            if (db.productoDao().getById(idProducto) != null) {
                val detalleApi = DetalleInsert(
                    idProducto,
                    cantidad,
                    precio
                )
                ventaProducto.add(detalleApi)
                reloadList()
            } else {
                Toast.makeText(
                    this,
                    "El producto con el id $idProducto no existe",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun loadForm() {
        val venta = db.ventaDao().getById(idVenta)
        val productos = db.ventaDao().getVentaProductos(idVenta)

        productos.forEach{
            val detalleApi = DetalleInsert(
                it.productoId,
                it.cantidad,
                it.precio
            )
            ventaProducto.add(detalleApi)
        }

        binding.txtNombreVenta.editText?.setText(venta?.nombre)
        binding.txtNitVenta.editText?.setText(venta?.nit.toString())
        binding.txtUsuarioVenta.editText?.setText(venta?.usuario)
    }

    private fun validarAgregarProducto(
        txtId: String,
        txtCantidad: String,
        txtPrecio: String
    ): Boolean {
        if (txtId.isEmpty()) {
            Toast.makeText(this, "El id del producto no puede estar vacio", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (txtCantidad.isEmpty()) {
            Toast.makeText(this, "La cantidad no puede estar vacia", Toast.LENGTH_SHORT).show()
            return false
        }

        if (txtPrecio.isEmpty()) {
            Toast.makeText(this, "El precio no puede estar vacio", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validarForm(nombre: String, nit: String, usuario: String): Boolean {
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nit.isEmpty()) {
            Toast.makeText(this, "El nit no puede estar vacio", Toast.LENGTH_SHORT).show()
            return false
        }

        if (usuario.isEmpty()) {
            Toast.makeText(this, "El usuario no puede estar vacio", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onVentaUpdateSuccess(venta: VentaApi, toast: Boolean) {
        val ventadb = Venta(
            venta.nombre,
            venta.nit.toLong(),
            venta.usuario
        )
        ventadb.ventaId = venta.id
        ventadb.created_at = venta.created_at
        ventadb.updated_at = venta.updated_at
        db.ventaDao().update(ventadb)
        if (toast) {
            Toast.makeText(this, "Venta actualizada con exito", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onVentaUpdateError(error: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onVentaInsert(venta: VentaApi) {
        val ventadb = Venta(
            venta.nombre,
            venta.nit.toLong(),
            venta.usuario
        )
        ventadb.ventaId = venta.id
        ventadb.created_at = venta.created_at
        ventadb.updated_at = venta.updated_at
        db.ventaDao().insert(ventadb)
        ventaProducto.forEach {
            val ventaProducto = VentaProducto(
                venta.id,
                it.id,
                it.cantidad,
                it.precio
            )
            db.ventaDao().insertProductoVendido(ventaProducto)
        }
        Toast.makeText(this, "Venta registrada con exito", Toast.LENGTH_SHORT).show()
    }

    override fun onVentaInsertError(error: Throwable) {
        TODO("Not yet implemented")
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