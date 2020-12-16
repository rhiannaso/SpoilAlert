package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MemberRecyclerAdapter(private val memberNames : MutableList<String>,
                            private val btnVisibility : MutableList<String>,
                            private val currUser : String,
                            private val currHouse : String,
                            private val originalActivity : MainActivity) : RecyclerView.Adapter<MemberRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemName: TextView = itemView.findViewById(R.id.member_name)
        var itemBtn: Button = itemView.findViewById(R.id.leave_btn)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.members_card, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.itemName.text = memberNames[i]
        val database = Firebase.database
        val myRef_houses = database.getReference("houses")
        // onClick for leave house button
        viewHolder.itemBtn.setOnClickListener {
            myRef_houses.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    var subRef = myRef_houses.child(currHouse)
                    subRef.child(currUser).removeValue()
                    val houseIdView = originalActivity.findViewById<TextView>(R.id.house_id)
                    houseIdView.text = ""
                    val navigationView = originalActivity.findViewById<View>(R.id.nav_view) as NavigationView
                    val nav_menu: Menu = navigationView.menu
                    nav_menu.findItem(R.id.nav_join_house).isVisible = true
                    nav_menu.findItem(R.id.nav_house_fridge).isVisible = false
                    nav_menu.findItem(R.id.nav_members).isVisible = false
                    nav_menu.findItem(R.id.nav_bulletin).isVisible = false
                    memberNames.removeAt(i)
                    notifyItemRemoved(i)
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("In leave house", "Failed to read value.", error.toException())
                }
            })
        }
        if (btnVisibility[i] == "visible") {
            viewHolder.itemBtn.visibility = View.VISIBLE
        } else {
            viewHolder.itemBtn.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return memberNames.size
    }
}