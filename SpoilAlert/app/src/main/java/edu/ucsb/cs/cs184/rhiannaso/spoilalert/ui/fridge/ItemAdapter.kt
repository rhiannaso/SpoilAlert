package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.fridge

import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import java.util.*

class ItemAdapter(private val itemList : MutableList<ItemCard>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private fun compressExpiration(expiration : String) : String {
        return expiration.substring(4, 10) + ", " + expiration.substring(24, 28)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        Log.d("adapter", "in onBindViewHolder adapter")
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val cur_item = itemList[position]
        holder.item_name.setText(cur_item.item_name)
        holder.item_quantity.setText("Quantity: " + cur_item.item_quantity)
        Log.d("cur_item.expiration", cur_item.item_expiration.toString())
        holder.item_expiration.setText(compressExpiration(cur_item.item_expiration.toString()))

        Log.d("adapter", "in onBindViewHolder adapter")
    }

    override fun getItemCount(): Int {
        Log.d("itemList size", itemList.size.toString())
        return itemList.size
    }

    class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val item_name : TextView = itemView.findViewById(R.id.item_name)
        val item_quantity : TextView = itemView.findViewById(R.id.item_quantity)
        val item_expiration : TextView = itemView.findViewById(R.id.item_expiration)
    }
}