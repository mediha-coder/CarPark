package com.example.myapplication.indoor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SlideshowFragment : Fragment(), SensorEventListener {

    // ---- Capteurs PDR ----
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var magnetometer: Sensor? = null
    private var pressureSensor: Sensor? = null

    // ---- Données PDR ----
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var heading = 0f
    private var stepLength = 24f
    private var azimuth: Float = 0f // Direction en degrés
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // ---- Filtre de Kalman ----
    private var p = 0.5f
    private val q = 0.0005f
    private val r = 0.2f




    // ---- Position filtrée ----
    private var lastXFiltered = 0f
    private var lastYFiltered = 0f
    private val smoothingFactor = 0.7f

    private val trajectory = mutableListOf<Pair<Float, Float>>()

    private lateinit var pdrView: SlideshowViewModel

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.indoor, container, false)
        pdrView = view.findViewById(R.id.pdrView)

        view.findViewById<Button>(R.id.zoomInButton)?.setOnClickListener {
            pdrView.zoomIn()
        }

        val distanceTextView = view.findViewById<TextView>(R.id.distanceText)

        pdrView.onDistanceChanged = { distanceInMeters ->
            val distanceInCm = (distanceInMeters * 100).toInt()
            distanceTextView.text = "Distance : $distanceInCm pas"
        }


        view.findViewById<Button>(R.id.saveButton)?.setOnClickListener {
            pdrView.saveTrajectoryToFile(requireContext(), "trajectoire")
        }

        view.findViewById<Button>(R.id.loadButton)?.setOnClickListener {
            pdrView.loadTrajectoryFromFile(context=requireContext(),"trajectoire")
        }

        view.findViewById<Button>(R.id.zoomOutButton)?.setOnClickListener {
            pdrView.zoomOut()
        }
        val retourButton = view.findViewById<Button>(R.id.btnRetour)

        retourButton.setOnClickListener {
            pdrView.enableRetourMode()
        }

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        return view
    }
    override fun onResume() {
        super.onResume()
        registerSensors()
    }
    override fun onPause() {
        super.onPause()
        unregisterSensors()
    }
    private fun registerSensors() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        pressureSensor?.let {sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity = event.values.clone()
                if (detectStep()) {
                    //applyKalmanFilter()
                    pdrView.addStep(azimuth)
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic = event.values.clone()
            }
            Sensor.TYPE_GYROSCOPE -> {
                updateHeading()
            }
        }
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            azimuth = (azimuth + 360) % 360  // azimut positif
        }
        val direction = getCardinalDirection(azimuth)
        pdrView.updateHeadingText(direction, azimuth.toInt())
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    private var lastMagnitude = 0f
    private var stepDetected = false

    private fun detectStep(): Boolean {
        // Calculer la magnitude du vecteur accélération
        val ax = gravity[0]
        val ay = gravity[1]
        val az = gravity[2]
        val magnitude = sqrt(ax * ax + ay * ay + az * az)
        val threshold = 11f
        if (!stepDetected && magnitude > threshold && lastMagnitude <= threshold) {
            stepDetected = true
            lastMagnitude = magnitude

            return true // Pas détecté !
        }

        // Reset quand magnitude redescend en dessous du seuil
        if (stepDetected && magnitude < threshold) {
            stepDetected = false
        }

        lastMagnitude = magnitude
        return false
    }


    private fun updateHeading() {
        val rm = FloatArray(9)
        val im = FloatArray(9)
        if (SensorManager.getRotationMatrix(rm, im, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rm, orientation)
            heading = Math.toDegrees(orientation[0].toDouble()).toFloat()
        }
    }

    private fun applyKalmanFilter() {
        val dx = (stepLength * cos(Math.toRadians(heading.toDouble()))).toFloat()
       val dy = (stepLength * sin(Math.toRadians(heading.toDouble()))).toFloat()

        // Kalman Filter - Mise à jour de l'estimation
        p += q
        val k = p / (p + r)

        lastXFiltered += k * (dx - lastXFiltered)
        lastYFiltered += k * (dy - lastYFiltered)
        p *= (1 - k)

        // Appliquer un léger lissage
        val smoothedX = smoothingFactor * lastXFiltered + (1 - smoothingFactor) * dx
        val smoothedY = smoothingFactor * lastYFiltered + (1 - smoothingFactor) * dy

        // Mise à jour de la vue
       pdrView.updatePosition(smoothedX, smoothedY)
        Log.d("PDR", "Position: ($smoothedX, $smoothedY) Heading: $heading")

        trajectory.add(Pair(smoothedX, smoothedY))
    }


    private fun getCardinalDirection(degree: Float): String {
        return when {
            degree >= 337.5 || degree < 22.5 -> "Nord"
            degree >= 22.5 && degree < 67.5 -> "Nord-Est"
            degree >= 67.5 && degree < 112.5 -> "Est"
            degree >= 112.5 && degree < 157.5 -> "Sud-Est"
            degree >= 157.5 && degree < 202.5 -> "Sud"
            degree >= 202.5 && degree < 247.5 -> "Sud-Ouest"
            degree >= 247.5 && degree < 292.5 -> "Ouest"
            else -> "Nord-Ouest"
        }
    }
    /*private fun detectStep() {
            val accMagnitude = sqrt(
                gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2]
            )
            if (accMagnitude > 11) {
                applyKalmanFilter()
            }
        }*/
}
