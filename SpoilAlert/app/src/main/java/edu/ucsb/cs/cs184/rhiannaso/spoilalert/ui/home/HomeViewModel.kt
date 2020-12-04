package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        //value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    //calculates the expiration date given current time and shelf life
    public fun calculateExpiration(shelfLife: Int) : String {
        var c : Calendar = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, shelfLife)
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        val format = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a")
        return format.format(c.time)
    }

}