package com.taso.tasotool

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RowAdapter(
    private var rows: MutableList<LedgerRow>,
    private val onUpdate: (LedgerRow) -> Unit,
    private val onDelete: (LedgerRow) -> Unit,
    private val onFocusNumbers: (EditText, LedgerRow) -> Unit
) : RecyclerView.Adapter<RowAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val editName: EditText = view.findViewById(R.id.edit_name)
        val editNumbers: EditText = view.findViewById(R.id.edit_numbers)
        val textSum: TextView = view.findViewById(R.id.text_sum)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val row = rows[position]

        if (payloads.isNotEmpty()) {
            // Partial update: only update the sum
            calculateSum(holder.textSum, row.numbers)
            return
        }

        holder.editName.setText(row.name)
        holder.editNumbers.setText(row.numbers)
        calculateSum(holder.textSum, row.numbers)

        // Name change listener
        holder.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                row.name = s.toString()
                onUpdate(row)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Numbers focus listener
        holder.editNumbers.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                onFocusNumbers(holder.editNumbers, row)
            }
        }
        
        // Prevent system keyboard from showing (redundant but safe)
        holder.editNumbers.showSoftInputOnFocus = false

        holder.btnDelete.setOnClickListener {
            onDelete(row)
        }
    }

    override fun getItemCount() = rows.size

    fun calculateSum(textView: TextView, rawValue: String) {
        val parts = rawValue.split(",")
        var sum = 0.0
        parts.forEach {
            val num = it.trim().toDoubleOrNull()
            if (num != null) sum += num
        }
        textView.text = if (sum % 1.0 == 0.0) sum.toInt().toString() else "%.2f".format(sum)
    }
}
