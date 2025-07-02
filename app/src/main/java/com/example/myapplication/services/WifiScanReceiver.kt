package com.example.myapplication.services


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast

class WifiScanReceiver(
    private val onAccessPointDetected: (ScanResult) -> Unit
) : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == intent.action) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val results = wifiManager.scanResults

            for (scanResult in results) {
                // Vérifie si le SSID ou le BSSID correspond à ton point d’accès cible
                if (scanResult.SSID == "MonWiFiCible" || scanResult.BSSID == "00:11:22:33:44:55") {
                    Log.d("WifiScanReceiver", "Point d'accès détecté : ${scanResult.SSID}")
                    Toast.makeText(context, "AP détecté : ${scanResult.SSID}", Toast.LENGTH_SHORT).show()
                    onAccessPointDetected(scanResult)
                    // Tu peux unregister le receiver si c’est ponctuel
                    break
                }
            }
        }
    }
}
