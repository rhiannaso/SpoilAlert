package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FridgeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
//        value = "Fridge is empty"
    }
    val text: LiveData<String> = _text
}