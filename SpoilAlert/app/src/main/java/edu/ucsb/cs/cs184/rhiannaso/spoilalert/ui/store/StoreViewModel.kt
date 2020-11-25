package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StoreViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Find a nearby store"
    }
    val text: LiveData<String> = _text
}