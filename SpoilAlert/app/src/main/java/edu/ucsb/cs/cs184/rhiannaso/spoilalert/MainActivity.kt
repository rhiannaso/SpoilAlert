package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.members.MembersFragment
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var currUser: String
    private lateinit var currHouse: String
    private val currRequestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val landingIntent = Intent(applicationContext, LandingActivity::class.java)
        startActivityForResult(landingIntent, currRequestCode)

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        fab.visibility = View.GONE // TODO/TEMP: while we don't need FAB
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_fridge,
                R.id.nav_store,
                R.id.nav_join_house,
                R.id.nav_members
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        createNotificationChannel()

    }

    fun getCurrUser() : String {
        return currUser
    }

    fun getCurrHouse() : String {
        return currHouse
    }

    fun setCurrHouse(house: String) {
        currHouse = house
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            currRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        currUser = data.getStringExtra("user_id")!!
                    }
                    checkIfInHouse()
                }
            }
        }
    }

    fun checkIfInHouse() {
        val database = Firebase.database
        val myRef_houses = database.getReference("houses")
        myRef_houses.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val houses = dataSnapshot.children
                val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
                val houseIdView = findViewById<TextView>(R.id.house_id)
                val nav_menu: Menu = navigationView.menu
                for (house in houses) {
                    val allUsers = house.children
                    for (user in allUsers) {
                        if (user.key == currUser) {
                            Log.i("checkIfInHouse", "User is in house")
                            houseIdView.text = "In House: ${house.key}"
                            currHouse = house.key.toString()
                            nav_menu.findItem(R.id.nav_join_house).isVisible = false
                            nav_menu.findItem(R.id.nav_house_fridge).isVisible = true
                            nav_menu.findItem(R.id.nav_members).isVisible = true
                        } else {
                            Log.i("checkIfInHouse", "User not in house")
                            houseIdView.text = ""
                            nav_menu.findItem(R.id.nav_join_house).isVisible = true
                            nav_menu.findItem(R.id.nav_house_fridge).isVisible = false
                            nav_menu.findItem(R.id.nav_members).isVisible = false
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("checkIfInHouse", "Failed to read value.", error.toException())
            }
        })
    }

    fun onClickSubmit(v: View) {
        var text_view = findViewById<EditText>(R.id.editTextTextPersonName)
        var text = text_view.getText().toString()
        Log.d("input text", text)
        var uuid = UUID.randomUUID()
        Log.d("UUID", uuid.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    //notificationSetup
    private val CHANNEL_ID = "spoil_alert_id"
    private val notificationId = 101

    private fun createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = "Hello World"
            val descriptionText = "I am birthed"
            val importance:Int = NotificationManager.IMPORTANCE_DEFAULT
            val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, name, importance).apply{
                description= "I am born"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification()
    {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kitchen_24px)
                .setContentTitle("7am")
                .setContentText("the usual morning line up")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(notificationId, builder.build())
        }
    }
}