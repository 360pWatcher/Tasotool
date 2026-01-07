package com.taso.tasotool

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: RowAdapter
    private var rows = mutableListOf<LedgerRow>()
    private var activeEditText: EditText? = null
    private var activeRow: LedgerRow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "tasotool-db")
            .fallbackToDestructiveMigration()
            .build()

        setupRecyclerView()
        setupFab()
        setupKeyboard()
        loadData()
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = RowAdapter(rows, 
            onUpdate = { row -> updateRow(row) },
            onDelete = { row -> confirmDelete(row) },
            onFocusNumbers = { editText, row -> showKeyboard(editText, row) }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            addNewRow()
        }
    }

    private fun setupKeyboard() {
        val container = findViewById<View>(R.id.keyboard_container)
        val btnDone = findViewById<Button>(R.id.btn_done)
        btnDone.setOnClickListener { hideKeyboard() }

        val keys = mapOf(
            R.id.key_0 to "0", R.id.key_1 to "1", R.id.key_2 to "2", R.id.key_3 to "3",
            R.id.key_4 to "4", R.id.key_5 to "5", R.id.key_6 to "6", R.id.key_7 to "7",
            R.id.key_8 to "8", R.id.key_9 to "9", R.id.key_dot to ".", R.id.key_minus to "-",
            R.id.key_comma to ","
        )

        keys.forEach { (id, value) ->
            findViewById<Button>(id).setOnClickListener { appendToActive(value) }
        }

        findViewById<Button>(R.id.key_backspace).setOnClickListener { backspace() }
        findViewById<Button>(R.id.key_next).setOnClickListener { appendToActive(",") }
    }

    private fun addNewRow() {
        CoroutineScope(Dispatchers.IO).launch {
            val newRow = LedgerRow(name = "New Item", numbers = "")
            val id = db.rowDao().insert(newRow)
            val insertedRow = newRow.copy(id = id.toInt())
            withContext(Dispatchers.Main) {
                rows.add(insertedRow)
                adapter.notifyItemInserted(rows.size - 1)
            }
        }
    }

    private fun updateRow(row: LedgerRow) {
        CoroutineScope(Dispatchers.IO).launch {
            db.rowDao().update(row)
        }
    }

    private fun confirmDelete(row: LedgerRow) {
        AlertDialog.Builder(this)
            .setTitle("Delete row?")
            .setMessage("Are you sure you want to delete ${row.name}?")
            .setPositiveButton("Delete") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.rowDao().delete(row)
                    withContext(Dispatchers.Main) {
                        val index = rows.indexOfFirst { it.id == row.id }
                        if (index != -1) {
                            rows.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.IO).launch {
            val data = db.rowDao().getAll()
            withContext(Dispatchers.Main) {
                rows.clear()
                rows.addAll(data)
                if (rows.isEmpty()) addNewRow()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showKeyboard(editText: EditText, row: LedgerRow) {
        activeEditText = editText
        activeRow = row
        findViewById<View>(R.id.keyboard_container).visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        findViewById<View>(R.id.keyboard_container).visibility = View.GONE
        activeEditText?.clearFocus()
        activeEditText = null
        activeRow = null
    }

    private fun appendToActive(value: String) {
        activeEditText?.let { et ->
            val start = et.selectionStart
            val end = et.selectionEnd
            et.editableText.replace(start, end, value)
            
            activeRow?.let { row ->
                row.numbers = et.text.toString()
                updateRow(row)
                
                // Trigger sum update in UI (this is a bit hacky, normally we'd use LiveData/Flow)
                val index = rows.indexOfFirst { it.id == row.id }
                if (index != -1) adapter.notifyItemChanged(index, "PAYLOAD_SUM")
            }
        }
    }

    private fun backspace() {
        activeEditText?.let { et ->
            val start = et.selectionStart
            val end = et.selectionEnd
            if (start > 0 || start != end) {
                if (start == end) {
                    et.editableText.delete(start - 1, start)
                } else {
                    et.editableText.delete(start, end)
                }
            }
            activeRow?.let { row ->
                row.numbers = et.text.toString()
                updateRow(row)
                val index = rows.indexOfFirst { it.id == row.id }
                if (index != -1) adapter.notifyItemChanged(index, "PAYLOAD_SUM")
            }
        }
    }
}
