package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.house_fridge

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R

class HouseFridgeFragment : Fragment() {

    companion object {
        fun newInstance() = HouseFridgeFragment()
    }

    private lateinit var viewModel: HouseFridgeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_house_fridge, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HouseFridgeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}