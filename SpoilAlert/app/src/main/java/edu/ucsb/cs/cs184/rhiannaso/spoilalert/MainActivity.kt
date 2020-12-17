package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
        fab.visibility = View.GONE
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_join_house
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
                        // Get information of current user
                        currUser = data.getStringExtra("user_id")!!
                    }
                    checkIfInHouse()
                }
            }
        }
    }

    private fun checkIfInHouse() {
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
                            // Display appropriate tabs for when user is in house
                            nav_menu.findItem(R.id.nav_join_house).isVisible = false
                            nav_menu.findItem(R.id.nav_house_fridge).isVisible = true
                            nav_menu.findItem(R.id.nav_members).isVisible = true
                            nav_menu.findItem(R.id.nav_bulletin).isVisible = true
                            return
                        } else {
                            Log.i("checkIfInHouse", "User not in house")
                            houseIdView.text = ""
                            // Display appropriate tabs for when user is not in house
                            nav_menu.findItem(R.id.nav_join_house).isVisible = true
                            nav_menu.findItem(R.id.nav_house_fridge).isVisible = false
                            nav_menu.findItem(R.id.nav_members).isVisible = false
                            nav_menu.findItem(R.id.nav_bulletin).isVisible = false
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

    private fun createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = "Hello World"
            val importance:Int = NotificationManager.IMPORTANCE_DEFAULT
            val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, name, importance).apply{
                description= "I am born"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}