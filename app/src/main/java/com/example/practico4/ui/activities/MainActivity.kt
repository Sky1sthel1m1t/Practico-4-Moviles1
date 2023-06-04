package com.example.practico4.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.practico4.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEventListeners()
    }

    private fun setupEventListeners() {
        binding.btnProductos.setOnClickListener {
            val intent = Intent(this, ProductoListActivity::class.java)
            startActivity(intent)
        }
        binding.btnCategorias.setOnClickListener {
            val intent = Intent(this, CategoriaListActivity::class.java)
            startActivity(intent)
        }
        binding.btnVentas.setOnClickListener {
            val intent = Intent(this, VentaListActivity::class.java)
            startActivity(intent)
        }
    }
}