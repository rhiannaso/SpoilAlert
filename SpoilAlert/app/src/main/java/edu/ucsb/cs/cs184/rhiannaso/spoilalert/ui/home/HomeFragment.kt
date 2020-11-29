package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // val textView: TextView = root.findViewById(R.id.text_home)
        // homeViewModel.text.observe(viewLifecycleOwner, Observer {
        // textView.text = it
        // })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var button = requireActivity().findViewById<Button>(R.id.submit_button)
        button.setOnClickListener {
            var text_view = requireActivity().findViewById<EditText>(R.id.editTextTextPersonName)
            var text = text_view.getText().toString()
            Log.d("input text", text)
            var uuid = UUID.randomUUID()
            Log.d("UUID", uuid.toString())
            val database = Firebase.database
            val myRef = database.getReference("items")

            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    if (dataSnapshot.hasChild(text)) {
                        Log.d("input", dataSnapshot.child(text).child("iid").value.toString())
                    }
                    else {
                        Log.d("input", "input item does not exist in items table")
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("In surfaceCreated", "Failed to read value.", error.toException())
                }
            })

            myRef.child("bananas").child("iid").setValue(uuid.toString())
        }
    }

}