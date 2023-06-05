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
import com.example.practico4.dal.dto.Venta
import com.example.practico4.dal.dto.VentaProducto
import com.example.practico4.databinding.ActivityVentaListBinding
import com.example.practico4.models.*
import com.example.practico4.repositories.VentaRepository
import com.example.practico4.ui.adapters.VentaListAdapter
import java.text.SimpleDateFormat

class VentaListActivity : AppCompatActivity(), VentaListAdapter.VentaListListener,
    VentaRepository.VentaApiListListener, VentaRepository.VentaApiUpdateListener,
    VentaRepository.VentaApiInsertListener {

    private lateinit var binding: ActivityVentaListBinding
    private lateinit var db: AppDatabase
    private val formatter: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaListBinding.inflate(layoutInflater)
        db = AppDatabase.getInstance(this)
        setContentView(binding.root)

        setupRecyclerView()
        setupEventListeners()
    }

    override fun onResume() {
        super.onResume()
        if (isOnline(this)) {
            VentaRepository.fetchListaVentas(this)
        }
        reloadList()
    }

    private fun reloadList() {
        binding.rvVentas.adapter?.let {
            (it as VentaListAdapter).reload(db.ventaDao().getAll())
        }
    }

    private fun setupEventListeners() {
        binding.fabAddVenta.setOnClickListener {
            val intent = Intent(this, VentaDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val ventas = db.ventaDao().getAll()
        binding.rvVentas.apply {
            adapter = VentaListAdapter(ventas, this@VentaListActivity)
            layoutManager = LinearLayoutManager(this@VentaListActivity)
        }
    }

    private fun eliminarVentasEliminadas(lista: List<VentaApi>) {
        val listaIds = lista.map { it.id }
        val ultimaFechaApi = formatter.parse(lista.last().created_at)
        DeletedModels.getInstance().deletedVentas.forEach {
            if (listaIds.contains(it.ventaId)) {
                val fechaVenta = formatter.parse(it.created_at)
                if (fechaVenta.before(ultimaFechaApi) || fechaVenta == ultimaFechaApi) {
                    val venta = it.ventaId?.let { it1 -> db.ventaDao().getById(it1) }
                    venta?.let { it1 -> VentaRepository.deleteVenta(it1, this, false) }
                }
            }
        }
        DeletedModels.getInstance().deletedVentas.clear()
        reloadList()
    }

    private fun insertVentasPorInsertar(
        ventasPorInsertar: ArrayList<Venta>,
        productosPorinsertar: HashMap<Int, List<VentaProducto>>
    ) {
        ventasPorInsertar.forEach {
            val ventaApi = parseToVentaApiInsert(it, productosPorinsertar[it.ventaId]!!)
            VentaRepository.insertVenta(ventaApi, this)
        }
    }

    private fun verificarDeleteInsert(idsVentasApi: ArrayList<Int>, ultimaVentaApi: VentaApi) {
        val listVentas = db.ventaDao().getVentaByNotIn(idsVentasApi)
        val ultimaFecha = formatter.parse(ultimaVentaApi.created_at)
        listVentas.forEach {
            val fechaVenta = formatter.parse(it.created_at)
            val productos = db.ventaDao().getVentaProductos(it.ventaId!!)
            deleteVentaProductos(it)
            db.ventaDao().delete(it)
            if (fechaVenta.after(ultimaFecha)) {
                val ventaApiInsert = parseToVentaApiInsert(it, productos)
                insertVenta(ventaApiInsert)
            }
        }
    }

    private fun verificarActualizacion(ventaApi: VentaApi, ventadb: Venta) {
        val fechaApi = formatter.parse(ventaApi.updated_at)
        val fechaDB = formatter.parse(ventadb.updated_at)
        if (fechaApi.after(fechaDB)) {
            ventadb.nombre = ventaApi.nombre
            ventadb.nit = ventaApi.nit.toLong()
            ventadb.usuario = ventaApi.usuario
            ventadb.updated_at = ventaApi.updated_at
            db.ventaDao().update(ventadb)
        } else {
            VentaRepository.updateVenta(ventadb, this, false)
        }
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

    private fun deleteVenta(venta: Venta) {
        if (isOnline(this)) {
            VentaRepository.deleteVenta(venta, this, true)
        } else {
            DeletedModels.getInstance().deletedVentas.add(venta)
            deleteVentaProductos(venta)
            db.ventaDao().delete(venta)
        }
    }

    private fun deleteVentaProductos(venta: Venta) {
        val productos = venta.ventaId?.let { db.ventaDao().getVentaProductos(it) }
        productos?.forEach {
            db.ventaDao().deleteProductoVendido(it)
        }
    }

    override fun onVentaClick(venta: Venta) {
        val intent = Intent(this, VentaDetailActivity::class.java)
        intent.putExtra("idVenta", venta.ventaId)
        startActivity(intent)
    }

    override fun onVentaDeleteClick(venta: Venta) {
        deleteVenta(venta)
    }

    override fun onVentaListFetched(ventas: List<VentaApi>) {
        val ventasPorInsertar = ArrayList<Venta>()
        val ventaProductoPorInsertar = HashMap<Int, List<VentaProducto>>()
        val idsVentasApi = ArrayList<Int>()

        eliminarVentasEliminadas(ventas)
        for (ventaApi in ventas) {
            val ventaDb = Venta(
                ventaApi.nombre,
                ventaApi.nit.toLong(),
                ventaApi.usuario
            )
            ventaDb.ventaId = ventaApi.id
            ventaDb.created_at = ventaApi.created_at
            ventaDb.updated_at = ventaApi.updated_at
            idsVentasApi.add(ventaApi.id)
            try {
                if (db.ventaDao().getAllIds().contains(ventaApi.id)
                    && db.ventaDao().getVentaProductos(ventaApi.id).size != ventaApi.detalle.size
                ) {
                    val productos = ArrayList<DetalleInsert>()
                    deleteVentaProductos(ventaDb)
                    ventaApi.detalle.forEach {
                        val detalle = DetalleInsert(
                            it.producto.id,
                            it.cantidad,
                            it.precio
                        )
                        productos.add(detalle)
                    }
                    insertVentaProducto(productos, ventaApi.id.toLong())
                }
                db.ventaDao().insert(ventaDb)
            } catch (e: Exception) {
                val ventaPorInsertar = db.ventaDao().getById(ventaApi.id)

                if (ventaDb.created_at == ventaPorInsertar?.created_at
                    && ventaDb.updated_at == ventaPorInsertar.updated_at
                ) {
                    continue
                }

                if (ventaDb.updated_at != ventaPorInsertar?.updated_at
                    && ventaDb.created_at == ventaPorInsertar?.created_at
                ) {
                    verificarActualizacion(ventaApi, ventaPorInsertar)
                    continue
                }

                ventaPorInsertar?.let {
                    ventasPorInsertar.add(it)
                    val productos = db.ventaDao().getVentaProductos(it.ventaId!!)
                    ventaProductoPorInsertar.put(it.ventaId!!, productos)
                }
                db.ventaDao().update(ventaDb)
                updateVentaProductos(ventaDb.ventaId!!, ventaApi.detalle)
            }
        }

        verificarDeleteInsert(idsVentasApi, ventas.last())
        insertVentasPorInsertar(ventasPorInsertar, ventaProductoPorInsertar)
        reloadList()
    }

    private fun updateVentaProductos(ventaId: Int, productos: List<DetalleApi>) {
        val productosDB = db.ventaDao().getVentaProductos(ventaId)
        productosDB.forEach {
            db.ventaDao().deleteProductoVendido(it)
        }
        productos.forEach {
            val ventaProducto = VentaProducto(
                ventaId,
                it.producto.id,
                it.cantidad,
                it.precio
            )
            db.ventaDao().insertProductoVendido(ventaProducto)
        }
    }

    private fun parseToVentaApiInsert(
        venta: Venta,
        productos: List<VentaProducto>
    ): VentaApiInsert {
        val productosApi = ArrayList<DetalleInsert>()
        productos.forEach {
            val detalle = DetalleInsert(
                it.productoId,
                it.cantidad,
                it.precio
            )
            productosApi.add(detalle)
        }
        return VentaApiInsert(
            venta.nombre,
            venta.nit.toString(),
            venta.usuario,
            productosApi
        )
    }

    override fun onVentaListFetchError(error: Throwable) {
        Toast.makeText(this, "Ha habido un error al obtener la lista", Toast.LENGTH_SHORT).show()
    }

    override fun onVentaDeleteSuccess(venta: Venta, toast: Boolean) {
        deleteVentaProductos(venta)
        db.ventaDao().delete(venta)
        reloadList()
        if (toast) {
            Toast.makeText(this, "Venta eliminada correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onVentaDeleteError(error: Throwable) {
        Toast.makeText(this, "Ha habido un error al eliminar la venta", Toast.LENGTH_SHORT).show()
    }

    override fun onVentaUpdateSuccess(venta: VentaApi, toast: Boolean) {
        Log.d("VentaListActivity", "Venta " + venta.id + " actualizada correctamente")
    }

    override fun onVentaUpdateError(error: Throwable) {
        Log.d(
            "VentaListActivity",
            "Ha habido un error al actualizar la venta, error: " + error.message
        )
    }

    override fun onVentaInsert(venta: VentaApi) {
        val ventaDb = Venta(
            venta.nombre,
            venta.nit.toLong(),
            venta.usuario
        )
        ventaDb.ventaId = venta.id
        ventaDb.created_at = venta.created_at
        ventaDb.updated_at = venta.updated_at
        db.ventaDao().insert(ventaDb)
        val detalleInsert = List(venta.detalle.size) {
            DetalleInsert(
                venta.detalle[it].producto.id,
                venta.detalle[it].cantidad,
                venta.detalle[it].precio
            )
        }
        insertVentaProducto(detalleInsert, venta.id.toLong())
        reloadList()
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