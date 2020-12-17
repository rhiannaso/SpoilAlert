package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.members

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.MainActivity
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.MemberRecyclerAdapter
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R

class MembersFragment : Fragment() {

    companion object {
        fun newInstance() = MembersFragment()
    }

    private lateinit var viewModel: MembersViewModel
    private var memberNames = mutableListOf<String>()
    private var btnVisibility = mutableListOf<String>()
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<MemberRecyclerAdapter.ViewHolder>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_members, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MembersViewModel::class.java)
        val currUser = (activity as MainActivity).getCurrUser()
        val currHouse = (activity as MainActivity).getCurrHouse()
        val recyclerView = requireActivity().findViewById<RecyclerView>(R.id.recycler_view)
        layoutManager = LinearLayoutManager(activity as Context)
        recyclerView.layoutManager = layoutManager

        val database = Firebase.database
        val myRef_houses = database.getReference("houses").child(currHouse)
        // Retrieve all users' names
        myRef_houses.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val allUsers = dataSnapshot.children
                for (user in allUsers) {
                    if(user.key != "messages") {
                        memberNames.add(user.value.toString())
                        // If user is current user, allow leave functionality
                        if (user.key.toString() == currUser) {
                            btnVisibility.add("visible")
                        } else { // Else, keep leave button hidden
                            btnVisibility.add("invisible")
                        }
                    }
                }
                // Pass in information to recycler view adapter
                adapter = MemberRecyclerAdapter(memberNames, btnVisibility, currUser, currHouse, (activity as MainActivity))
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("In get member names", "Failed to read value.", error.toException())
            }
        })
    }
}