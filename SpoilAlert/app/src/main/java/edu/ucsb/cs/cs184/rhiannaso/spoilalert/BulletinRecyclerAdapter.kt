package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BulletinRecyclerAdapter (private val names : MutableList<String>,
                               private val dates : MutableList<String>,
                               private val messages : MutableList<String>) : RecyclerView.Adapter<BulletinRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.bulletin_name)
        var date: TextView = itemView.findViewById(R.id.bulletin_date)
        var message: TextView = itemView.findViewById(R.id.bulletin_text)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.bulletin_card, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.name.text = names[i]
        viewHolder.date.text = dates[i]
        viewHolder.message.text = messages[i]
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}