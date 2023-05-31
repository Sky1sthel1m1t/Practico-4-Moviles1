package com.example.practico4.ui.activities

import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Producto
import com.example.practico4.databinding.ActivityProductoDetailBinding
import java.text.SimpleDateFormat

class ProductoDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductoDetailBinding
    private lateinit var db: AppDatabase
    private val formatter: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    private var idProducto: Int = -1

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
        val fecha = formatter.format(Calendar.getInstance().time).toString()

        if (!validarCategoriaExista(categoria_id)) {
            Toast.makeText(this, "La categoria no existe", Toast.LENGTH_SHORT).show()
            return
        }

        val producto = Producto(nombre, descrpcion, precio, categoria_id)

        if (idProducto == -1) {
            producto.created_at = fecha
            producto.updated_at = fecha
            db.productoDao().insert(producto)
        } else {
            producto.productoId = idProducto
            producto.updated_at = fecha
            db.productoDao().update(producto)
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

    private fun validarCategoriaExista(categoria_id : Int) : Boolean {
        return db.categoriaDao().getAllId().contains(categoria_id)
    }
}