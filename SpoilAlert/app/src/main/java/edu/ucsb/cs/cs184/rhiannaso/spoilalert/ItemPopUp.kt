package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import java.util.*


class ItemPopUp {

    fun showItemPopUp(view: View, itemName: String, nid: Int, expiration: Date) {
        Log.d("", "in popup view")
        //Create a View object yourself through inflater
        val inflater = LayoutInflater.from(view.context)
        val popupView: View = inflater.inflate(R.layout.pop_up_layout, null)

        //Specify the length and width through constants
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT

        //Make Inactive Items Outside Of PopupWindow
        val focusable = true

        //Create a window with our parameters
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        //Initialize the elements of our window, install the handler
        val name = popupView.findViewById<TextView>(R.id.item_name_popup)
        name.text = itemName

        //Displays custom reminder options
        val dropdown = popupView.findViewById<Spinner>(R.id.reminder_spinner)
        var reminder_options = arrayOf("On Expiration Date", "1 Day Before Expiration",
                                        "2 Days Before Expiration", "3 Days Before Expiration")
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item, reminder_options)
        dropdown.adapter = adapter

        //save button
        val saveChangesButton: Button = popupView.findViewById(R.id.item_options_save_btn)
        saveChangesButton.setOnClickListener(View.OnClickListener {
            Log.d("", "Selected Index: ${dropdown.selectedItemPosition}")
            cancelAlarm(view, nid)
            setAlarm(view, itemName, nid, expiration, dropdown.selectedItemPosition)
            Toast.makeText(view.context, "Options Saved!", Toast.LENGTH_SHORT).show()
        })

        val card = popupView.findViewById<CardView>(R.id.item_info_card)

        //Needed to ensure clicking on the card doesn't close the popupWindow
        card.setOnClickListener { }

        //Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener { v, event -> //Close the window when clicked
            popupWindow.dismiss()
            true
        }
    }

    fun cancelAlarm(view: View, nid: Int) {
        val alarmManager = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val myIntent = Intent(view.context,
                NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                view.context, nid, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.cancel(pendingIntent)
    }

    fun setAlarm(view: View, itemName: String, nid: Int, expiration: Date, reminderOption: Int) {
        val c = Calendar.getInstance()
        c.time = expiration
        c.add(Calendar.DAY_OF_YEAR, -1 * reminderOption)

        val CHANNEL_ID = "spoil_alert_id"
        val n = NotificationCompat.Builder(view.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kitchen_24px)
                .setContentTitle("$itemName expiring soon!")
                .setContentText("$itemName expiring within a day! Try to eat them if you can!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationIntent = Intent(view.context, NotificationPublisher::class.java)
        notificationIntent.putExtra("notification-id", nid)
        notificationIntent.putExtra("notification", n.build())
        val pIntent = PendingIntent.getBroadcast(view.context, nid, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val futureInMillis = c.timeInMillis
        val aM = view.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        aM.set(AlarmManager.RTC_WAKEUP, futureInMillis, pIntent)
    }
}