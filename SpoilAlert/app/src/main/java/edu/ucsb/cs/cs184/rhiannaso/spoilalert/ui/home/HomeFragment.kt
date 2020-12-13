package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.view.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.LandingActivity
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.NotificationPublisher
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var signout_button : Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        /*val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        signout_button = requireActivity().findViewById(R.id.signout_button)

        // signout button intent
        signout_button.setOnClickListener {
            AuthUI.getInstance()
                .signOut(activity as Context)
                .addOnCompleteListener {
                    // ...
                }
            val drawerLayout: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
            drawerLayout.closeDrawer(Gravity.START as Int)
            val landingIntent = Intent(
                requireActivity().applicationContext,
                LandingActivity::class.java
            )
            requireActivity().startActivityForResult(landingIntent, 0)
        }

        var text_view = requireActivity().findViewById<AutoCompleteTextView>(R.id.editItem)
        val languages = resources.getStringArray(R.array.Languages)
        val adapter = context?.let {
            ArrayAdapter(it,
                android.R.layout.simple_list_item_1, languages)
        }
        text_view.setAdapter(adapter)

        var button = requireActivity().findViewById<Button>(R.id.submit_button)
        // onClick for submit item button
        button.setOnClickListener {
            var text_view = requireActivity().findViewById<EditText>(R.id.editItem)

            var quantity_view = requireActivity().findViewById<EditText>(R.id.editQuantity)
            var text = text_view.getText().toString()
            var quantity = quantity_view.getText().toString()

            if (quantity.trim().length <= 0 || text.trim().length <= 0) {
                if (quantity.trim().length <= 0 && text.trim().length <= 0)
                    Toast.makeText(context, "Please include item and quantity", Toast.LENGTH_LONG).show()
                else if (text.trim().length <= 0)
                    Toast.makeText(context, "Please include item", Toast.LENGTH_SHORT).show()
                else if (quantity.trim().length <= 0)
                    Toast.makeText(context, "Please include quantity", Toast.LENGTH_LONG).show()
            } else {
                Log.d("input text", text)
                Log.d("input quantity", quantity.toString())

                val database = Firebase.database
                val myRef_items = database.getReference("items")
                val myRef_users = database.getReference("users")

                myRef_items.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if (dataSnapshot.hasChild(text)) {
                            Log.d("input",
                                    dataSnapshot.child(text)
                                            .child("name").value.toString() + "for user: " + FirebaseAuth.getInstance().uid.toString()
                            )
                            // generate new uid for entry if the item is in the db items table
                            var uuid = UUID.randomUUID()
                            Log.d("UUID", uuid.toString())
                            // add item name to users item log
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("name").setValue(text)
                            // add item iid to users item log
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("iid").setValue(dataSnapshot.child(text).child("iid").value.toString())
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("eid").setValue(uuid.toString())
                            // add item expiration date (current time + shelf life) to users item log
                            var shelfLife = dataSnapshot.child(text).child("shelf_life").value.toString().toInt()
                            var expiration = viewModel.calculateExpiration(shelfLife)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("expiration_date").setValue(expiration)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("quantity").setValue(quantity)
                            //Id used for notification and requestCode
                            val nid = setNotificationTime(text, expiration)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("nid").setValue(nid)


                            text_view.setText(null)
                            quantity_view.setText(null)

                            var msg = quantity + " " + text + "(s) added to your fridge!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                        } else {
                            Log.d("input", "input item does not exist in items table")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("In surfaceCreated", "Failed to read value.", error.toException())
                    }
                })
            }
        }
    }
  
    private val CHANNEL_ID = "spoil_alert_id"
    private val notificationId = 101
    private fun sendNotification()
    {
        val builder = context?.let {
            NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kitchen_24px)
                .setContentTitle("7am")
                .setContentText("the usual morning line up")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        if (builder != null) {
            with(context?.let { NotificationManagerCompat.from(it) }){
                this?.notify(notificationId, builder.build())
            }
        }
    }

    //sets an alarm to send a notification
    private fun setNotificationTime(itemName: String, date: String) : Int {
        val format = SimpleDateFormat("EEEE, MM/dd/yyyy 'at' h:mm a")
        var d: Date = Date()
        try {
            d = format.parse(date)
        } catch (e : ParseException) {
            Log.d("HomeFragment", e.message!!)
        }

        val c = Calendar.getInstance()
        //nid will be used for both notification-id along with requestCode
        val nid = Integer.parseInt(SimpleDateFormat("ddHHmmssSS", Locale.US).format(c.time))

        //create notification
        val n = NotificationCompat.Builder(requireActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kitchen_24px)
                .setContentTitle("$itemName expiring soon!")
                .setContentText("$itemName expiring within a day! Try to eat them if you can!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        //create intent and pendingIntent
        val notificationIntent = Intent(activity, NotificationPublisher::class.java)
        notificationIntent.putExtra("notification-id", nid)
        notificationIntent.putExtra("notification", n.build())
        val pIntent = PendingIntent.getBroadcast(activity, nid, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //set alarm
        c.time = d
//        c.add(Calendar.SECOND, 5)
        val futureInMillis = c.timeInMillis
        val aM = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        aM.set(AlarmManager.RTC_WAKEUP, futureInMillis, pIntent)

        return nid
    }
}