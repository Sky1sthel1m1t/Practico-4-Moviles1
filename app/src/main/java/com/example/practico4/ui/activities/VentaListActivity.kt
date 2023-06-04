package com.example.practico4.ui.activities

import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Venta
import com.example.practico4.databinding.ActivityVentaListBinding
import com.example.practico4.models.VentaApi
import com.example.practico4.repositories.VentaRepository
import com.example.practico4.ui.adapters.VentaListAdapter
import java.text.SimpleDateFormat

class VentaListActivity : AppCompatActivity(), VentaListAdapter.VentaListListener,
    VentaRepository.VentaApiListListener, VentaRepository.VentaApiUpdateListener {

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
        VentaRepository.fetchListaVentas(this)
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

    override fun onVentaClick(venta: Venta) {
        val intent = Intent(this, VentaDetailActivity::class.java)
        intent.putExtra("idVenta", venta.ventaId)
        startActivity(intent)
    }

    override fun onVentaDeleteClick(venta: Venta) {
        VentaRepository.deleteVenta(venta, this, true)
    }

    override fun onVentaListFetched(ventas: List<VentaApi>) {
        val ventasPorInsertar = ArrayList<Venta>()
        val idsVentasApi = ArrayList<Int>()
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

                ventaPorInsertar?.let { ventasPorInsertar.add(it) }
                db.ventaDao().update(ventaDb)
            }
        }

        verificarDelete(idsVentasApi)
        insertProductosPorInsertar(ventasPorInsertar)
        reloadList()
    }

    private fun insertProductosPorInsertar(ventasPorInsertar: ArrayList<Venta>) {
        ventasPorInsertar.forEach {
            val fecha = formatter.format(Calendar.getInstance().time).toString()
            it.created_at = fecha
            it.updated_at = fecha
            db.ventaDao().insert(it)
        }
    }

    private fun verificarDelete(idsVentasApi: ArrayList<Int>) {
        val listVentas = db.ventaDao().getVentaByNotIn(idsVentasApi)
        listVentas.forEach {
            db.ventaDao().delete(it)
        }
    }

    private fun verificarActualizacion(ventaApi: VentaApi, ventadb: Venta) {
        val fechaApi = formatter.parse(ventaApi.updated_at)
        val fechaDB = formatter.parse(ventadb.updated_at)
        if (fechaApi.after(fechaDB)) {
            ventadb.nombre = ventaApi.nombre
            ventadb.updated_at = ventaApi.updated_at
            db.ventaDao().update(ventadb)
        } else {
            VentaRepository.updateVenta(ventadb, this, false)
        }
    }

    override fun onVentaListFetchError(error: Throwable) {
        Toast.makeText(this, "Ha habido un error al obtener la lista", Toast.LENGTH_SHORT).show()
    }

    override fun onVentaDeleteSuccess(venta: Venta, toast: Boolean) {
        if (toast) {
            db.ventaDao().delete(venta)
            Toast.makeText(this, "Venta eliminada correctamente", Toast.LENGTH_SHORT).show()
            reloadList()
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
}