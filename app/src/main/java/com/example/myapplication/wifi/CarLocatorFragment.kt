package com.example.myapplication.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


@Suppress("DEPRECATION")

class CarLocatorFragment : Fragment() {

    private val LOCATION_PERMISSION_REQUEST = 1

    private lateinit var wifiManager: WifiManager
    private lateinit var textView: TextView
    private lateinit var scanButton: Button
    private lateinit var signalProgressBar: ProgressBar
    private lateinit var lineChart: LineChart
    /*
    private var savedBssid: String = ""
    private var savedRssi: Int = -100
    private var isScanning = false

    private val entries = mutableListOf<Entry>()
    private var xValue = 0f

   private val handler = Handler(Looper.getMainLooper())
    private val scanInterval = 20000L // 10 secondes



    private val scanRunnable = object : Runnable {
        override fun run() {
            checkProximity()
            handler.postDelayed(this, scanInterval)
        }
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isScanning) return

            val results = wifiManager.scanResults

            val result = results.find { it.BSSID == savedBssid }

            val currentRssi = result?.level ?: -100
            updateChart(currentRssi)

            val proximity = (currentRssi + 100).coerceIn(0, 100)
            signalProgressBar.progress = proximity

            val trend = when {
                currentRssi - savedRssi > 5 -> "You're getting closer 📶"
                currentRssi - savedRssi < -5 -> "You're moving in the wrong direction🏃‍♂️"
                else -> " Stable Distance"
            }

            textView.text = if (result != null)
                "Signal : $currentRssi dBm\n$trend"
            else
                "Network lost"
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wifi, container, false)

        textView = view.findViewById(R.id.textViewWifi)
        scanButton = view.findViewById(R.id.btnScan)
        signalProgressBar = view.findViewById(R.id.signalProgressBar)
        lineChart = view.findViewById(R.id.lineChart)

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        setupChart()

        scanButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
            } else {
                startWifiScan()
            }
        }

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val cardWifiInfo = view.findViewById<CardView>(R.id.cardWifiInfo)


        btnBack.setOnClickListener {
            cardWifiInfo.visibility = View.GONE
            textView.visibility = View.VISIBLE
            lineChart.visibility = View.VISIBLE
            isScanning = true
            handler.post(scanRunnable)
        }

        return view
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = -100f
            axisLeft.axisMaximum = 0f
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startWifiScan() {
        val success = wifiManager.startScan()

        if (!success) {
            textView.text = "scan Wi-Fi failed "
            return
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            textView.text = "Permission manquante"
            return
        }

        val results: List<ScanResult> = wifiManager.scanResults
        if (results.isEmpty()) {
            textView.text = "No network detected"
            return
        }

        val bestNetwork = results.maxByOrNull { it.level }
        bestNetwork?.let {
            savedBssid = it.BSSID
            savedRssi = it.level
            textView.visibility = View.GONE
            lineChart.visibility = View.GONE
            view?.findViewById<CardView>(R.id.cardWifiInfo)?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.textBestNetwork)?.text =
                "📶 Best network : ${it.SSID}\n🔗 BSSID : ${it.BSSID}\n📡 RSS : ${it.level} dBm"

            isScanning = true
            val sharedPreferences = requireContext().getSharedPreferences("wifi_data", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("saved_bssid", it.BSSID)
            editor.putInt("saved_rssi", it.level)
            editor.apply()

            handler.post(scanRunnable)
        } ?: run {
            textView.text = "Pas de réseau disponible"
        }
    }

    private fun checkProximity() {
        val sharedPreferences = requireContext().getSharedPreferences("wifi_data", Context.MODE_PRIVATE)
        savedBssid = sharedPreferences.getString("saved_bssid", "") ?: ""
        savedRssi = sharedPreferences.getInt("saved_rssi", -100)

        if (!isScanning || savedBssid.isEmpty()) return

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            textView.text = "Permission manquante"
            return
        }

        wifiManager.startScan() // Résultats reçus via BroadcastReceiver
    }

    private fun updateChart(rssi: Int) {
        entries.add(Entry(xValue, rssi.toFloat()))
        xValue += 1f

        val dataSet = LineDataSet(entries, "RSS (dBm)").apply {
            setDrawCircles(false)
            color = Color.BLUE
            lineWidth = 2f
        }

        lineChart.data = LineData(dataSet)
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        if (isScanning) handler.post(scanRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(scanRunnable)
        requireContext().unregisterReceiver(wifiScanReceiver)
    }

    @SuppressLint("SetTextI18n")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startWifiScan()
        } else {
            textView.text = "Permission refusée"
        }
    }
}*/


    private val handler = Handler(Looper.getMainLooper())
    private val scanInterval = 10000L

    private var isScanPending = false
    private var isScanning = false
    private var savedBssid = ""
    private var savedRssi = -100
    private var xValue = 0f
    private val entries = ArrayList<Entry>()

    private val scanRunnable = object : Runnable {
        override fun run() {
            checkProximity()
            handler.postDelayed(this, scanInterval)
        }
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isScanning) return

            isScanPending = false

            val results = wifiManager.scanResults
            val result = results.find { it.BSSID == savedBssid }

            val currentRssi = result?.level ?: -100
            updateChart(currentRssi)

            val proximity = (currentRssi + 100).coerceIn(0, 100)
            signalProgressBar.progress = proximity

            val trend = when {
                currentRssi - savedRssi > 5 -> "You're getting closer 📶"
                currentRssi - savedRssi < -5 -> "You're moving in the wrong direction 🏃‍♂️"
                else -> "Stable Distance"
            }

            textView.text = result?.let {
                "Signal: $currentRssi dBm\n$trend"
            } ?: "Network lost"
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wifi, container, false)

        textView = view.findViewById(R.id.textViewWifi)
        scanButton = view.findViewById(R.id.btnScan)
        signalProgressBar = view.findViewById(R.id.signalProgressBar)
        lineChart = view.findViewById(R.id.lineChart)

        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        setupChart()

        scanButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            } else {
                startWifiScan()
            }
        }

        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val cardWifiInfo = view.findViewById<CardView>(R.id.cardWifiInfo)

        btnBack.setOnClickListener {
            cardWifiInfo.visibility = View.GONE
            textView.visibility = View.VISIBLE
            lineChart.visibility = View.VISIBLE
            isScanning = true
            handler.post(scanRunnable)
        }

        return view
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = -100f
            axisLeft.axisMaximum = 0f
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startWifiScan() {
        val success = wifiManager.startScan()
        if (!success) {
            textView.text = "Scan Wi-Fi failed .Wait a few seconds."
            return
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            textView.text = "Permission denied"
            return
        }

        val results = wifiManager.scanResults
        if (results.isEmpty()) {
            textView.text = "No network detected"
            return
        }

        val bestNetwork = results.maxByOrNull { it.level }
        bestNetwork?.let {
            savedBssid = it.BSSID
            savedRssi = it.level
            textView.visibility = View.GONE
            lineChart.visibility = View.GONE
            view?.findViewById<CardView>(R.id.cardWifiInfo)?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.textBestNetwork)?.text =
                "📶 Best network : ${it.SSID}\n🔗 BSSID : ${it.BSSID}\n📡 RSS : ${it.level} dBm"

            isScanning = true

            val sharedPreferences =
                requireContext().getSharedPreferences("wifi_data", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("saved_bssid", it.BSSID)
                putInt("saved_rssi", it.level)
                apply()
            }

            handler.post(scanRunnable)
        } ?: run {
            textView.text = "No network available"
        }
    }

    private fun checkProximity() {
        val sharedPreferences =
            requireContext().getSharedPreferences("wifi_data", Context.MODE_PRIVATE)
        savedBssid = sharedPreferences.getString("saved_bssid", "") ?: ""
        savedRssi = sharedPreferences.getInt("saved_rssi", -100)

        if (!isScanning || savedBssid.isEmpty()) return

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            textView.text = "Permission manquante"
            return
        }

        if (!isScanPending) {
            isScanPending = true
            val success = wifiManager.startScan()
            if (!success) {
                isScanPending = false
                textView.text = "Échec du scan Wi-Fi (trop rapide ?)"
            }
        }
    }

    private fun updateChart(rssi: Int) {
        entries.add(Entry(xValue, rssi.toFloat()))
        xValue += 1f

        val dataSet = LineDataSet(entries, "RSS (dBm)").apply {
            setDrawCircles(false)
            color = Color.BLUE
            lineWidth = 2f
        }

        lineChart.data = LineData(dataSet)
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        if (isScanning) handler.post(scanRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(scanRunnable)
        requireContext().unregisterReceiver(wifiScanReceiver)
    }

    @SuppressLint("SetTextI18n")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startWifiScan()
        } else {
            textView.text = "Permission refusée"
        }
    }
}


