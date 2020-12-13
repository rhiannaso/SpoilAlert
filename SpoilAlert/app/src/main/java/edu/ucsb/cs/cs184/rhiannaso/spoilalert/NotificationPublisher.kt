package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val nM = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val n = intent.getParcelableExtra<Notification>("notification")
        val id = intent.getIntExtra("notification-id", 0)
        nM.notify(id, n)
    }
}