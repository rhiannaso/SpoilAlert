package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.join_house

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.MainActivity
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import java.util.*

class JoinHouseFragment : Fragment() {

    companion object {
        fun newInstance() = JoinHouseFragment()
    }

    private lateinit var viewModel: JoinHouseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_join_house, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(JoinHouseViewModel::class.java)
        // TODO: Use the ViewModel
        val database = Firebase.database
        val myRef_houses = database.getReference("houses")

        val create_button = requireActivity().findViewById<ImageButton>(R.id.create_btn)
        // onClick for create house button
        create_button.setOnClickListener {
            val username = requireActivity().findViewById<EditText>(R.id.create_username)
            val username_input = username.text.toString()
            val err_msg = requireActivity().findViewById<TextView>(R.id.create_err_msg)
            val success_msg = requireActivity().findViewById<TextView>(R.id.create_success)
            myRef_houses.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    if (dataSnapshot.hasChild(username_input)) {
                        Log.i("Create House", "House already exists")
                        err_msg.visibility = View.VISIBLE
                        success_msg.visibility = View.INVISIBLE
                    } else {
                        Log.i("Create House", "House does not yet exist")
                        val given_house = myRef_houses.child(username_input)
                        given_house.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).setValue(FirebaseAuth.getInstance().currentUser?.displayName.toString())
                        success_msg.visibility = View.VISIBLE
                        err_msg.visibility = View.INVISIBLE
                        // Hide create/join tab once in a house
                        val navigationView = requireActivity().findViewById<View>(R.id.nav_view) as NavigationView
                        val nav_menu: Menu = navigationView.menu
                        nav_menu.findItem(R.id.nav_join_house).isVisible = false
                        nav_menu.findItem(R.id.nav_members).isVisible = true
                        // Indicate that user is in house
                        val houseIdView = requireActivity().findViewById<TextView>(R.id.house_id)
                        houseIdView.text = "In House: $username_input"
                        (activity as MainActivity).setCurrHouse(username_input)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("In create house", "Failed to read value.", error.toException())
                }
            })
        }

        val join_button = requireActivity().findViewById<ImageButton>(R.id.join_btn)
        // onClick for join house button
        join_button.setOnClickListener {
            val username = requireActivity().findViewById<EditText>(R.id.join_username)
            val username_input = username.text.toString()
            val success_msg = requireActivity().findViewById<TextView>(R.id.join_success)
            val err_msg = requireActivity().findViewById<TextView>(R.id.join_err_msg)
            myRef_houses.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    if (dataSnapshot.hasChild(username_input)) {
                        Log.i("Join House", "Joining House")
                        val given_house = myRef_houses.child(username_input)
                        given_house.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).setValue(FirebaseAuth.getInstance().currentUser?.displayName.toString())
                        success_msg.visibility = View.VISIBLE
                        err_msg.visibility = View.INVISIBLE
                        // Hide create/join tab once in a house
                        val navigationView = requireActivity().findViewById<View>(R.id.nav_view) as NavigationView
                        val nav_menu: Menu = navigationView.menu
                        nav_menu.findItem(R.id.nav_join_house).isVisible = false
                        nav_menu.findItem(R.id.nav_members).isVisible = true
                        // Indicate that user is in house
                        val houseIdView = requireActivity().findViewById<TextView>(R.id.house_id)
                        houseIdView.text = "In House: $username_input"
                        (activity as MainActivity).setCurrHouse(username_input)
                    } else {
                        Log.i("Join House", "House does not exist with given username")
                        err_msg.visibility = View.VISIBLE
                        success_msg.visibility = View.INVISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("In join house", "Failed to read value.", error.toException())
                }
            })
        }
    }

}