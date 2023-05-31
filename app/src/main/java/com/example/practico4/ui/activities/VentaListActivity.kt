package com.example.practico4.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Venta
import com.example.practico4.databinding.ActivityVentaListBinding
import com.example.practico4.ui.adapters.VentaListAdapter

class VentaListActivity : AppCompatActivity(), VentaListAdapter.VentaListListener {

    private lateinit var binding: ActivityVentaListBinding
    private lateinit var bd : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVentaListBinding.inflate(layoutInflater)
        bd = AppDatabase.getInstance(this)
        setContentView(binding.root)

        setupRecyclerView()
        setupEventListeners()
    }

    override fun onResume() {
        super.onResume()
        binding.rvVentas.adapter?.let {
            (it as VentaListAdapter).reload(bd.ventaDao().getAll())
        }
    }

    private fun setupEventListeners() {
        binding.fabAddVenta.setOnClickListener {
            val intent = Intent(this, VentaDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        val ventas = bd.ventaDao().getAll()
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
        venta.ventaId?.let { Log.d("Delete", bd.ventaDao().deleteProductosEnVenta(it).toString()) }
        bd.ventaDao().delete(venta)
        binding.rvVentas.adapter?.let {
            (it as VentaListAdapter).reload(bd.ventaDao().getAll())
        }
    }
}