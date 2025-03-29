package com.anand.hope

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class OfflineMeshNetwork(private val context: Context) {

    private var wifiP2pManager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null

    // ✅ Start the mesh network
    fun startMeshNetwork() {
        wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager?.initialize(context, context.mainLooper, null)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (context == null || intent == null) return
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        if (!hasPermissions()) return
                        wifiP2pManager?.requestPeers(channel) { peerList ->
                            peerList.deviceList.forEach { connectToPeer(it) }
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        }

        context.registerReceiver(receiver, intentFilter)
        discoverPeers()
    }

    // ✅ Discover available peers
    private fun discoverPeers() {
        wifiP2pManager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MeshNetwork", "Peer discovery started")
            }

            override fun onFailure(reason: Int) {
                Log.e("MeshNetwork", "Peer discovery failed: $reason")
            }
        })
    }

    // ✅ Connect to a discovered peer
    private fun connectToPeer(device: WifiP2pDevice) {
        if (!hasPermissions()) return
        val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
        wifiP2pManager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MeshNetwork", "Connected to peer: ${device.deviceName}")
            }

            override fun onFailure(reason: Int) {
                Log.e("MeshNetwork", "Failed to connect to peer: ${device.deviceName}")
            }
        })
    }

    // ✅ Request necessary permissions
    fun requestPermissions(activity: Activity?) {
        activity ?: return
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.addAll(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), 1)
    }

    // ✅ Stop the mesh network
    fun stopMeshNetwork() {
        receiver?.let { context.unregisterReceiver(it) }
        wifiP2pManager = null
        channel = null
    }

    // ✅ Send a message to connected peers
    fun sendMessage(message: String) {
        Log.d("MeshNetwork", "Sending message: $message")
    }

    // ✅ Check if required permissions are granted
    private fun hasPermissions(): Boolean {
        return listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU }
        ).all { permission ->
            permission == null || ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ✅ Check if the device is online
    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
    }
}