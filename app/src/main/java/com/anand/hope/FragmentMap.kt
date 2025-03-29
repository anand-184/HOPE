package com.anand.hope

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.Polyline
import java.net.HttpURLConnection
import java.net.URL
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration

class MapFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var sendLocationButton: Button
    private lateinit var offlineMeshNetwork: OfflineMeshNetwork
    private lateinit var bluetoothAdapter: BluetoothAdapter

    // Activity Result Launcher for Bluetooth
    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != -1) { // -1 means RESULT_OK
            Log.e("Bluetooth", "User denied enabling Bluetooth")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        mapView = view.findViewById(R.id.mapView)
        searchEditText = view.findViewById(R.id.etSearch)
        searchButton = view.findViewById(R.id.btnSearch)
        sendLocationButton = view.findViewById(R.id.btnSendLocation)

        // Configure Map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(28.6139, 77.2090)) // Default: New Delhi

        // Initialize Offline Mesh Network
        offlineMeshNetwork = OfflineMeshNetwork(requireContext())
        offlineMeshNetwork.startMeshNetwork()

        // Request Permissions
        requestPermissions()

        // Setup Bluetooth
        setupBluetoothMesh()

        // Search Button Click
        searchButton.setOnClickListener {
            val locationQuery = searchEditText.text.toString()
            if (locationQuery.isNotEmpty()) {
                fetchLocationData(locationQuery) { geoPoint ->
                    geoPoint?.let {
                        mapView.controller.setCenter(it)
                    }
                }
            }
        }

        // Send Current Location Over Mesh
        sendLocationButton.setOnClickListener {
            val currentLocation = mapView.mapCenter as GeoPoint
            offlineMeshNetwork.sendMessage("Location: ${currentLocation.latitude}, ${currentLocation.longitude}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        offlineMeshNetwork.stopMeshNetwork()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.addAll(
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        requestPermissions(permissions.toTypedArray(), 1)
    }

    private fun setupBluetoothMesh() {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
        scanForBluetoothDevices()
    }

    private fun scanForBluetoothDevices() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.BLUETOOTH_SCAN), 2)
            return
        }
        bluetoothAdapter.startDiscovery()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action: String? = intent?.action
                if (BluetoothDevice.ACTION_FOUND == action) {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    Log.d("BluetoothMesh", "Device found: ${device?.name}, ${device?.address}")
                }
            }
        }
        requireContext().registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    private fun fetchLocationData(query: String, callback: (GeoPoint?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://nominatim.openstreetmap.org/search?format=json&q=$query"
            val response = try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                connection.inputStream.bufferedReader().readText()
            } catch (e: Exception) {
                Log.e("OSM", "Error fetching location", e)
                null
            }
            withContext(Dispatchers.Main) {
                response?.let {
                    val jsonArray = JSONArray(it)
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val lat = obj.getDouble("lat")
                        val lon = obj.getDouble("lon")
                        callback(GeoPoint(lat, lon))
                    } else {
                        callback(null)
                    }
                }
            }
        }
    }

    private fun drawRoute(from: GeoPoint, to: GeoPoint) {
        val polyline = Polyline()
        polyline.addPoint(from)
        polyline.addPoint(to)
        polyline.color = Color.BLUE
        polyline.width = 5f
        mapView.overlayManager.add(polyline)
        mapView.invalidate()
    }
}
