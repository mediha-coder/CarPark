package com.example.myapplication.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

object LocationUtils {

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context, onLocationFixed: () -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.create()
            .apply {
                interval = 5000
                fastestInterval = 2000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }


        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location.accuracy < 20) {
                        // On a un fix GPS < 20m de précision
                        onLocationFixed()
                        // Optionnel : arrêter les updates
                        fusedLocationClient.removeLocationUpdates(this)
                        break
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}
