package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.bulletin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.*
import java.text.SimpleDateFormat
import java.util.*

class BulletinFragment : Fragment() {

    companion object {
        fun newInstance() = BulletinFragment()
    }

    private lateinit var viewModel: BulletinViewModel
    private var messages = mutableListOf<String>()
    private var dates = mutableListOf<String>()
    private var names = mutableListOf<String>()
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<BulletinRecyclerAdapter.ViewHolder>? = null
    private lateinit var recyclerView : RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bulletin, container, false)
    }

    fun getUserName(currUser: String) {
        val database = Firebase.database
        val myRef_user = database.getReference("users").child(currUser).child("name")
        myRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i("USERNAME", dataSnapshot.value.toString())
                return
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("In get user name", "Failed to read value.", error.toException())
            }
        })
    }

    fun checkForMessages() {
        val currHouse = (activity as MainActivity).getCurrHouse()
        val database = Firebase.database
        val myRef_messages = database.getReference("houses").child(currHouse).child("messages")
        myRef_messages.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dbMsgs = dataSnapshot.children
                names.clear()
                dates.clear()
                messages.clear()
                for (msg in dbMsgs) {
                    names.add(msg.child("name").value.toString())
                    dates.add(msg.child("date").value.toString())
                    messages.add(msg.child("msg").value.toString())
                }
                adapter = BulletinRecyclerAdapter(names, dates, messages)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("In check messages", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BulletinViewModel::class.java)
        val currUser = (activity as MainActivity).getCurrUser()
        val currHouse = (activity as MainActivity).getCurrHouse()
        val database = Firebase.database

        val send_btn = requireActivity().findViewById<ImageButton>(R.id.send_btn)
        recyclerView = requireActivity().findViewById(R.id.bulletin_recycler)
        layoutManager = LinearLayoutManager(activity as Context)
        (layoutManager as LinearLayoutManager).stackFromEnd = true
        (layoutManager as LinearLayoutManager).reverseLayout = false
        recyclerView.layoutManager = layoutManager

        val msgView = requireActivity().findViewById<EditText>(R.id.bulletin_msg)
        val myRef_user = database.getReference("users").child(currUser).child("name")
        val myRef_thisHouse = database.getReference("houses").child(currHouse).child("messages")
        send_btn.setOnClickListener {
            val msgText = msgView.text.toString()
            val c: Calendar = Calendar.getInstance()
            val msgKey = c.timeInMillis
            val df = SimpleDateFormat("MM/dd/yyyy (HH:mm)")
            val msgDate: String = df.format(c.time)
            myRef_user.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userName = dataSnapshot.value.toString()
                    val msgToSend = BulletinMsg(userName, msgDate, msgText)
                    myRef_thisHouse.child(msgKey.toString()).setValue(msgToSend)
                    checkForMessages()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("In get user name", "Failed to read value.", error.toException())
                }
            })
            msgView.text = null
        }
        checkForMessages()
    }
}