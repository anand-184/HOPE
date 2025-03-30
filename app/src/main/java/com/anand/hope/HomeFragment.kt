package com.anand.hope

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anand.hope.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sosListAdapter: SOSListAdapter
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    var sosList= arrayListOf<SOSModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        firestore = FirebaseFirestore.getInstance()

        sosListAdapter= SOSListAdapter(sosList)

        binding?.needsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.needsRecyclerView?.adapter = sosListAdapter

        fetchSOSData()
        getUserLocation()


    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                userLatitude = it.latitude
                userLongitude = it.longitude
            }
        }
    }



    private fun fetchSOSData() {
        firestore.collection("sos_alerts").get().addOnSuccessListener { documents ->
            val sosList = mutableListOf<SOSModel>()
            for (document in documents) {
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val disasterType = document.getString("disasterType") ?: "Unknown"
                val needs = document.get("needs") as? List<String> ?: listOf()

                sosList.add(SOSModel(latitude.toString(), longitude.toString(), disasterType, needs))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
