package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ItemAdapter(private val itemList : MutableList<ItemCard>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private fun compressExpiration(expiration : String) : String {
        return expiration.substring(4, 10) + ", " + expiration.substring(24, 28)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val database = Firebase.database
        val myRef_item = database.getReference("users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(itemList[position].eid)
        val cur_item = itemList[position]
        holder.item_name.setText(cur_item.item_name)
        holder.item_quantity.setText(cur_item.item_quantity)
        Log.d("cur_item.expiration", cur_item.item_expiration.toString())
        holder.item_expiration.setText(compressExpiration(cur_item.item_expiration.toString()))
        holder.item_minus_button.setOnClickListener {
            if (itemList[position].item_quantity.toInt() > 1) {
                itemList[position].item_quantity = (itemList[position].item_quantity.toInt()-1).toString()
                myRef_item.child("quantity").setValue(itemList[position].item_quantity.toString())
                notifyItemChanged(position)
            }
        }
        holder.item_plus_button.setOnClickListener {
            itemList[position].item_quantity = (itemList[position].item_quantity.toInt()+1).toString()
            myRef_item.child("quantity").setValue(itemList[position].item_quantity.toString())
            notifyItemChanged(position)
        }
        holder.item_settings_button.setOnClickListener {
            // TODO: onClickListener for each item's settings
            // TODO: add popup window code here to be triggered on item_settings_button click
            Log.d("item_settings_button", "item" + position.toString() + " settings button clicked")
        }

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
        val item_minus_button : ImageButton = itemView.findViewById(R.id.minus_quantity)
        val item_plus_button : ImageButton = itemView.findViewById(R.id.plus_quantity)
        val item_settings_button : ImageView = itemView.findViewById(R.id.item_settings)
    }
}