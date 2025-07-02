package com.example.myapplication.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class SettingsFragment : Fragment() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchCompass: Switch
    private lateinit var seekBarSensitivity: SeekBar
    private lateinit var sensitivityValue: TextView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchWifi: Switch
    private lateinit var spinnerScanInterval: Spinner
    private lateinit var btnSaveSettings: Button

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.settings, container, false)

        // Initialisation des vues
        switchCompass = view.findViewById(R.id.switchCompass)
        seekBarSensitivity = view.findViewById(R.id.seekBarSensitivity)
        sensitivityValue = view.findViewById(R.id.sensitivityValue)
        switchWifi = view.findViewById(R.id.switchWifi)
        spinnerScanInterval = view.findViewById(R.id.spinnerScanInterval)
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings)

        // Chargement des préférences
        val prefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        switchCompass.isChecked = prefs.getBoolean("compass_enabled", true)
        seekBarSensitivity.progress = prefs.getInt("compass_sensitivity", 5)
        switchWifi.isChecked = prefs.getBoolean("wifi_autoscan", false)

        val savedInterval = prefs.getInt("scan_interval", 10).toString()

        // Initialisation du SeekBar
        sensitivityValue.text = seekBarSensitivity.progress.toString()
        seekBarSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sensitivityValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Initialisation du Spinner
        val intervalOptions = listOf("5", "10", "15", "30", "60")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervalOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerScanInterval.adapter = adapter

        val position = intervalOptions.indexOf(savedInterval)
        if (position != -1) {
            spinnerScanInterval.setSelection(position)
        }

        // Sauvegarde des préférences
        btnSaveSettings.setOnClickListener {
            val selectedInterval = spinnerScanInterval.selectedItem.toString().toIntOrNull() ?: 10

            val editor = prefs.edit()
            editor.putBoolean("compass_enabled", switchCompass.isChecked)
            editor.putInt("compass_sensitivity", seekBarSensitivity.progress)
            editor.putBoolean("wifi_autoscan", switchWifi.isChecked)
            editor.putInt("scan_interval", selectedInterval)
            editor.apply()

            Toast.makeText(requireContext(), "Paramètres enregistrés", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
