package com.chhue.gpstimeapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.Calendar
import java.util.TimeZone


class MainActivity : AppCompatActivity() {

    private var locationManager: LocationManager? = null
    val btnLoca by lazy { findViewById<Button>(R.id.button_location) }
    val btnTime by lazy { findViewById<Button>(R.id.button_time) }
    var location: Location? = null

    var latitude: Double = 37.5016375 // 선릉 사무실 위치...
    var longitude: Double = 127.041577

    // 퍼미션 요청 및 결과 받아오는 객체
    var permissionLauncher =
        registerForActivityResult<String, Boolean>(ActivityResultContracts.RequestPermission(),
            object : ActivityResultCallback<Boolean?> {
                override fun onActivityResult(result: Boolean?) {
                    result?.let {
                        Toast.makeText(this@MainActivity, "위치정보제공허용", Toast.LENGTH_SHORT).show()
                        return
                    }
                    Toast.makeText(this@MainActivity, "위치정보제공거부", Toast.LENGTH_SHORT).show()
                }
            })

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 위치 정보 관리자
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 위치정보 동적 퍼미션
        val checkPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (checkPermission == PackageManager.PERMISSION_DENIED) permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

        // 현재 위치
        btnLoca.setOnClickListener { findLocation() }

        // 위치 기반 시간
        btnTime.setOnClickListener { findTime() }

    }

    private fun findLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return  //명시적 퍼미션
        }


        locationManager!!.apply {
            when {
                isProviderEnabled("fused") -> location = getLastKnownLocation("fused")
                isProviderEnabled("gps") -> location = getLastKnownLocation("gps")
                isProviderEnabled("network") -> location = getLastKnownLocation("network")
            }
        }
//
//        if (locationManager!!.isProviderEnabled("fused")) { // 위치정보 permission 필요
//            location = locationManager!!.getLastKnownLocation("fused")
//        } else if (locationManager!!.isProviderEnabled("gps")) {
//            location = locationManager!!.getLastKnownLocation("gps")
//        } else if (locationManager!!.isProviderEnabled("network")) {
//            location = locationManager!!.getLastKnownLocation("network")
//        }

        location?.let {
            latitude = location!!.latitude
            longitude = location!!.longitude
        }
        Toast.makeText(this@MainActivity, "$latitude, $longitude", Toast.LENGTH_SHORT).show()

    }

    private fun findTime() {
        location?.let {
            latitude = location!!.latitude
            longitude = location!!.longitude
        }
        val timeZone = getTimeZone(latitude, longitude)
        val currentTime = getCurrentTime(timeZone)
        Toast.makeText(this@MainActivity, currentTime, Toast.LENGTH_SHORT).show()
    }

    private fun getTimeZone(latitude: Double, longitude: Double): TimeZone {
        return TimeZone.getTimeZone(
            TimeZone.getAvailableIDs((longitude * 10000).toInt()).firstOrNull { it.contains('/') }
                ?: TimeZone.getDefault().id)
    }

    private fun getCurrentTime(timeZone: TimeZone): String {
        val calendar: Calendar = Calendar.getInstance(timeZone)
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)
        val second: Int = calendar.get(Calendar.SECOND)
        return String.format("%02d:%02d:%02d", hour, minute, second)
    }


}