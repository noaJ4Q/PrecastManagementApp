package com.example.precastmanagementapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ElementResultAdapter(
    private var items: List<PrecastElement> = emptyList(),
    private val onClickListener: ((element: PrecastElement) -> Unit)
) : RecyclerView.Adapter<ElementResultAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_element_result, parent, false)
        return ViewHolder(view, onClickListener)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<PrecastElement>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder (
        private val view: View,
        private val onClickListener: ((element: PrecastElement) -> Unit)
    ) : RecyclerView.ViewHolder (view)
    {
        fun bind(element: PrecastElement) {
            view.findViewById<TextView>(R.id.tvElementName).text = element.name
            view.findViewById<TextView>(R.id.tvElementCategory).text = element.category
            view.setOnClickListener { onClickListener.invoke(element) }
        }
    }
}