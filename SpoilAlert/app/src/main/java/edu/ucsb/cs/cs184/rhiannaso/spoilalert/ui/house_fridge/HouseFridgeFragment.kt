package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.house_fridge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.*
import java.text.SimpleDateFormat

class HouseFridgeFragment : Fragment() {

    companion object {
        fun newInstance() = HouseFridgeFragment()
    }

    private lateinit var viewModel: HouseFridgeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_house_fridge, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HouseFridgeViewModel::class.java)

        var item_list : MutableList<ItemCard> = ArrayList<ItemCard>()
        var adapter : ItemAdapterHouse = ItemAdapterHouse(item_list)

        val recycler_view = requireActivity().findViewById<RecyclerView>(R.id.recycler_view_house)
        val house_fridge_empty = requireActivity().findViewById<TextView>(R.id.house_fridge_empty)
        var house_users : MutableList<String> = ArrayList<String>()

        val curr_house = (activity as MainActivity).getCurrHouse()
        Log.d("curr_house", curr_house.toString())

        val database = Firebase.database
        val myRef_house = database.getReference("houses").child(curr_house)

        myRef_house.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (user in dataSnapshot.children) {
                    val myRef_user = database.getReference("users").child(user.key.toString()).child("items")
                    myRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val format = SimpleDateFormat("MM/dd/yyyy")
                                val item = ItemCard(snapshot.child("name").value.toString(), snapshot.child("quantity").value.toString(),
                                        format.parse(compressExpiration(snapshot.child("expiration_date").value.toString())),
                                        snapshot.child("eid").value.toString(), snapshot.child("nid").value.toString(),
                                        snapshot.child("owner").value.toString(), snapshot.child("owner_name").value.toString())
                                item_list.add(item)
                            }
                            item_list = item_list.sortedWith(compareBy({ it.item_expiration })).toMutableList()
                            adapter = ItemAdapterHouse(item_list)
                            if (item_list.size > 0) {
                                house_fridge_empty.visibility = TextView.INVISIBLE
                            }
                            else if (item_list.size == 0) {
                                house_fridge_empty.visibility = TextView.VISIBLE
                            }
                            Log.d("item_list_house", item_list.toString())
                            recycler_view.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
                Log.d("house_users", house_users.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        Log.d("item_list", item_list.toString())

        recycler_view.layoutManager = LinearLayoutManager(activity as Context)
        recycler_view.setHasFixedSize(true)
    }

    private fun compressExpiration(expiration : String) : String {
        return expiration.substring(expiration.length-22, expiration.length-12)
    }
}