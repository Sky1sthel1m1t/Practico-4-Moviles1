package com.example.practico4.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Producto
import com.example.practico4.dal.dto.ProductoConCategoria
import com.example.practico4.databinding.ActivityProductoListBinding
import com.example.practico4.ui.adapters.ProductoListAdapter

class ProductoListActivity : AppCompatActivity(), ProductoListAdapter.ProductoListListener {
    private lateinit var binding: ActivityProductoListBinding
    private lateinit var db: AppDatabase

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
        db.productoDao().delete(producto.producto)
        reloadList()
    }
}