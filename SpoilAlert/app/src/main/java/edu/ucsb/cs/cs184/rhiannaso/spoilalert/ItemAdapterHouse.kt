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

class ItemAdapterHouse(private val itemList : MutableList<ItemCard>) : RecyclerView.Adapter<ItemAdapterHouse.ItemHouseViewHolder>() {

    private fun compressExpiration(expiration : String) : String {
        return expiration.substring(4, 10) + ", " + expiration.substring(24, 28)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHouseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_card_house, parent, false)
        return ItemHouseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemHouseViewHolder, position: Int) {
        val database = Firebase.database
        val myRef_item = database.getReference("users").child(itemList[position].owner).child("items").child(itemList[position].eid)
        val cur_item = itemList[position]
        holder.item_name.setText(cur_item.item_name)
        holder.item_quantity.setText(cur_item.item_quantity)
        Log.d("cur_item.expiration", cur_item.item_expiration.toString())
        holder.item_expiration.setText(compressExpiration(cur_item.item_expiration.toString()))
        holder.item_owner.setText("Owner: " + cur_item.owner_name.toString())
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

        Log.d("adapter", "in onBindViewHolder adapter")
    }

    override fun getItemCount(): Int {
        Log.d("itemList size", itemList.size.toString())
        return itemList.size
    }

    class ItemHouseViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val item_name : TextView = itemView.findViewById(R.id.item_name_house)
        val item_quantity : TextView = itemView.findViewById(R.id.item_quantity_house)
        val item_expiration : TextView = itemView.findViewById(R.id.item_expiration_house)
        val item_minus_button : ImageButton = itemView.findViewById(R.id.minus_quantity_house)
        val item_plus_button : ImageButton = itemView.findViewById(R.id.plus_quantity_house)
        val item_owner : TextView = itemView.findViewById(R.id.item_owner_house)
    }
}