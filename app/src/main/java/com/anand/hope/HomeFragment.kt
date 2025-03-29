package com.anand.hope

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anand.hope.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    var binding: FragmentHomeBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sosListAdapter: SOSListAdapter
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        firestore = FirebaseFirestore.getInstance()

        sosListAdapter = SOSListAdapter()
        binding?.needsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.needsRecyclerView?.adapter = sosListAdapter

        fetchSOSData()
        getUserLocation()

        binding?.sosButton?.setOnClickListener {
            sendSOS()
        }
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
                val userLocation = "Lat: ${'$'}{it.latitude}, Lng: ${'$'}{it.longitude}"
                binding?.locationTextView?.text = userLocation
            }
        }
    }


    private fun sendSOS() {
        val locationText = binding?.locationTextView?.text?.toString()

        if (locationText.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract latitude and longitude using Regex
        val regex = """Lat: ([\d.-]+), Lng: ([\d.-]+)""".toRegex()
        val matchResult = regex.find(locationText)

        if (matchResult != null) {
            val latitude = matchResult.groupValues[1]
            val longitude = matchResult.groupValues[2]

            val sosData = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "needs" to listOf("Water", "Food", "Medical Assistance")
            )

            firestore.collection("sos_alerts").add(sosData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "SOS Sent!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to send SOS", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Invalid location format", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchSOSData() {
        firestore.collection("sos_alerts").get().addOnSuccessListener { documents ->
            val sosList = mutableListOf<SOSModel>()
            for (document in documents) {
                val latitude = document.getString("latitude") ?: "Unknown"
                val longitude = document.getString("longitude") ?: "Unknown"
                val needs = document.get("needs") as? List<String> ?: listOf()
                sosList.add(SOSModel(latitude, longitude, needs))
            }
            sosListAdapter.submitList(sosList)
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
