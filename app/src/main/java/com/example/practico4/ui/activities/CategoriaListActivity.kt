package com.example.practico4.ui.activities

import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practico4.dal.conn.AppDatabase
import com.example.practico4.dal.dto.Categoria
import com.example.practico4.databinding.ActivityCategoriaListBinding
import com.example.practico4.models.CategoriaApi
import com.example.practico4.models.DeleteResponse
import com.example.practico4.repositories.CategoriaRepository
import com.example.practico4.repositories.RetrofitRepository
import com.example.practico4.ui.adapters.CategoriaListAdapter
import java.text.SimpleDateFormat

class CategoriaListActivity : AppCompatActivity(), CategoriaListAdapter.CategoriaListListener,
    CategoriaRepository.CategoriaApiListener {

    private lateinit var binding: ActivityCategoriaListBinding
    private lateinit var db: AppDatabase
    private val formatter: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        setupRecyclerView()
        setupEventsListeners()
    }

    private fun setupEventsListeners() {
        binding.fabAddCategoria.setOnClickListener {
            addCategoria()
        }
    }

    private fun setupRecyclerView() {
        binding.rvCategoria.apply {
            adapter = CategoriaListAdapter(db.categoriaDao().getAll(), this@CategoriaListActivity)
            layoutManager = LinearLayoutManager(this@CategoriaListActivity).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CategoriaRepository.fetchListaCategorias(this)
        reloadList()
    }

    private fun reloadList() {
        binding.rvCategoria.adapter.let { adapter ->
            if (adapter is CategoriaListAdapter) {
                adapter.reload(db.categoriaDao().getAll())
            }
        }
    }

    private fun addCategoria() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ingrese un nombre para la categoria")
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton("Añadir") { dialog, _ ->
            if (input.text.toString().isNotBlank()) {
                val aux = Categoria(input.text.toString())
                CategoriaRepository.insertCategoria(aux, this)
                reloadList()
            } else {
                dialog.cancel()
                Toast.makeText(
                    this,
                    "El nombre de una categoria no puede estar vacio",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun insertCategoriasPorInsertar(lista: ArrayList<Categoria>) {
        lista.forEach {
            val fecha = formatter.format(Calendar.getInstance().time).toString()
            it.created_at = fecha
            it.updated_at = fecha
            db.categoriaDao().insert(it)
        }
    }

    private fun verificarActualizacion(categoriaApi: CategoriaApi, categoria: Categoria) {
        val fechaApi = formatter.parse(categoriaApi.updated_at)
        val fechaDB = formatter.parse(categoria.updated_at)
        if (fechaApi.after(fechaDB)) {
            categoria.nombre = categoriaApi.nombre
            categoria.updated_at = categoriaApi.updated_at
            db.categoriaDao().update(categoria)
        } else {
            CategoriaRepository.updateCategoria(categoria, this, false)
        }
    }

    private fun verificarDelete(ids : ArrayList<Int>){
        val listCategoria = db.categoriaDao().getCategoriaByNotIn(ids)
        listCategoria.forEach {
            db.categoriaDao().delete(it)
        }
    }

    override fun onCategoriaEditClick(categoria: Categoria) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ingrese el nuevo nombre de la categoria")
        val input = EditText(this)
        input.setText(categoria.nombre)
        builder.setView(input)
        builder.setPositiveButton("Guardar") { dialog, _ ->
            if (input.text.toString().isNotBlank()) {
                categoria.nombre = input.text.toString()
                CategoriaRepository.updateCategoria(categoria, this, true)
                reloadList()
            } else {
                dialog.cancel()
                Toast.makeText(
                    this,
                    "El nombre de una categoria no puede estar vacio",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    override fun onCategoriaDeleteClick(categoria: Categoria) {
        db.categoriaDao().delete(categoria)
        categoria.id?.let { CategoriaRepository.deleteCategoria(it, this) }
        reloadList()
    }

    override fun onCategoriaClick(categoria: Categoria) {
        val productos = categoria.id?.let { db.categoriaDao().getCategoriaProducto(it) }?.productos
        val productosNombres = productos?.size?.let {
            Array(it) {
                productos[it].toString()
            }
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Productos que tienen esta categoria")

        if (productosNombres?.size != 0) {
            builder.setItems(productosNombres, null)
        } else {
            builder.setMessage("No hay productos que tengan esta categoria")
        }

        builder.setNegativeButton("Aceptar") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    override fun onCategoriaListFetched(list: List<CategoriaApi>) {
        val categoriasPorInsertar = ArrayList<Categoria>()
        val idCategoriasApi = ArrayList<Int>()
        for (categoria in list) {
            val categoriaDB = Categoria(categoria.nombre)
            idCategoriasApi.add(categoria.id)
            categoriaDB.id = categoria.id
            categoriaDB.created_at = categoria.created_at
            categoriaDB.updated_at = categoria.updated_at
            try {
                db.categoriaDao().insert(categoriaDB)
            } catch (e: Exception) {
                val categoriaPorInsertar = db.categoriaDao().getById(categoria.id)

                if (categoriaDB.created_at == categoriaPorInsertar?.created_at
                    && categoriaDB.updated_at == categoriaPorInsertar.updated_at) {
                    continue
                }

                if (categoriaDB.updated_at != categoriaPorInsertar?.updated_at && categoriaDB.created_at == categoriaPorInsertar?.created_at) {
                    verificarActualizacion(categoria, categoriaPorInsertar)
                    continue
                }

                categoriaPorInsertar?.nombre?.let { Categoria(it) }
                    ?.let { categoriasPorInsertar.add(it) }
                db.categoriaDao().update(categoriaDB)
            }
        }
        verificarDelete(idCategoriasApi)
        insertCategoriasPorInsertar(categoriasPorInsertar)
        reloadList()
    }

    override fun onCategoriaListFetchError(error: Throwable) {
        Toast.makeText(this, "Error al obtener las categorias", Toast.LENGTH_SHORT).show()
    }

    override fun onCategoriaUpdateSuccess(categoriaApi: CategoriaApi, toast: Boolean) {
        if (toast) {
            val categoria = Categoria(categoriaApi.nombre)
            categoria.id = categoriaApi.id
            categoria.created_at = categoriaApi.created_at
            categoria.updated_at = categoriaApi.updated_at
            db.categoriaDao().update(categoria)
            reloadList()
            Toast.makeText(this, "Se ha actualizado la categoria correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("CategoriaListActivity", "Categoria actualizada: $categoriaApi")
        }
    }

    override fun onCategoriaUpdateError(error: Throwable) {
        Log.d("CategoriaListActivity", "Error al actualizar la categoria: $error")
    }

    override fun onCategoriaInsertSuccess(categoriaApi: CategoriaApi) {
        val categoria = Categoria(categoriaApi.nombre)
        categoria.id = categoriaApi.id
        categoria.created_at = categoriaApi.created_at
        categoria.updated_at = categoriaApi.updated_at
        db.categoriaDao().insert(categoria)
        reloadList()
    }

    override fun onCategoriaInsertError(error: Throwable) {
        Toast.makeText(this, "Error al insertar la categoria", Toast.LENGTH_SHORT).show()
    }

    override fun onCategoriaDeleteSuccess(respuesta: DeleteResponse) {
        Toast.makeText(this, "Se ha eliminado la categoria correctamente", Toast.LENGTH_SHORT).show()
    }

    override fun onCategoriaDeleteError(error: Throwable) {
        Toast.makeText(this, "Error al eliminar la categoria", Toast.LENGTH_SHORT).show()
    }
}