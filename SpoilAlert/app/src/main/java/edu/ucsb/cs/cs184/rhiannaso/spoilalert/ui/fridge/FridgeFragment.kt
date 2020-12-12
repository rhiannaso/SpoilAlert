package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.fridge

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.ItemAdapter
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.ItemCard
import java.text.SimpleDateFormat


class FridgeFragment : Fragment() {

    private lateinit var fridgeViewModel: FridgeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        fridgeViewModel =
                ViewModelProvider(this).get(FridgeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_fridge, container, false)
//        val textView: TextView = root.findViewById(R.id.text_fridge)
//        fridgeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var item_list : MutableList<ItemCard> = ArrayList<ItemCard>()
        var adapter : ItemAdapter = ItemAdapter(item_list)

        val recycler_view = requireActivity().findViewById<RecyclerView>(R.id.recycler_view)

        val database = Firebase.database
        val myRef_user = database.getReference("users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items")

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //Remove swiped item from list and notify the RecyclerView
                val position = viewHolder.adapterPosition
                myRef_user.child(item_list[position].eid).removeValue()
                Log.d("onSwiped", item_list.toString() + " " + position.toString())
                item_list.removeAt(position)
                adapter.notifyItemRemoved(position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    var itemView: View = viewHolder.itemView

                    var p: Paint = Paint()
                    p.setColor(Color.RED)
                    if (dX < 0) {
                        /* Set your color for negative displacement */

                        // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                        c.drawRect(
                            itemView.getRight().toFloat() + dX, itemView.getTop().toFloat(),
                            itemView.getRight().toFloat(), itemView.getBottom().toFloat(), p
                        );
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recycler_view);

        myRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    Log.d("in items snapshot", snapshot.getValue().toString())
                    val format = SimpleDateFormat("MM/dd/yyyy")
                    val item = ItemCard(snapshot.child("name").value.toString(), "1", format.parse(compressExpiration(snapshot.child("expiration_date").value.toString())), snapshot.child("eid").value.toString())
                    Log.d("in items snapshot", item.toString())
                    item_list.add(item)
                    Log.d("in items snapshot", item_list.toString())
                }
                Log.d("before sort", item_list.toString())
                item_list = item_list.sortedWith(compareBy({ it.item_expiration })).toMutableList()
                Log.d("after sort", item_list.toString())
                adapter = ItemAdapter(item_list)
                recycler_view.adapter = adapter
                adapter.notifyDataSetChanged()
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