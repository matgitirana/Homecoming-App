package com.example.homecoming

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.telephony.SmsManager
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: MarkerOptions
    private var destination: MarkerOptions? = null
    private lateinit var locationCallback: LocationCallback
    private var distance = -1.0
    private var phone = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                updateLocation(locationResult!!.lastLocation)

            }
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION),
                1)
        }


    }


    fun updateLocation(location: Location?) {
        if (location != null) {
            mMap.clear()

            var mp = MarkerOptions()
            mp.position(LatLng(location.latitude, location.longitude))
            mp.title("My position")
            currentLocation = mp
            mMap.addMarker(currentLocation)

            if (destination != null) {
                mMap.addMarker(destination)

                showBothMarkers()

                var thisLocation = Location("here")
                thisLocation.latitude = currentLocation.position.latitude
                thisLocation.longitude = currentLocation.position.longitude

                var destinationLocation = Location("Destination")
                destinationLocation.latitude = destination!!.position.latitude
                destinationLocation.longitude = destination!!.position.longitude

                var currentDistance = thisLocation.distanceTo(destinationLocation)

                Toast.makeText(this, currentDistance.toString(), Toast.LENGTH_LONG).show()

                if(currentDistance<=distance){
                    val smsManager = SmsManager.getDefault() as SmsManager
                    smsManager.sendTextMessage(phone, null, "I am almost there", null, null)
                    Toast.makeText(this, "Arrived", Toast.LENGTH_LONG).show()
                    phone = ""
                    distance = -1.0
                    destination = null
                }
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude), 16F))
            }



        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1)
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                var mp = MarkerOptions()
                mp.position(LatLng(location.latitude, location.longitude))
                mp.title("My position")
                currentLocation = mp
                mMap.addMarker(currentLocation)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }

        var locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)


    }

    override fun onResume() {
        super.onResume()
        listLayout.removeAllViews()
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        val allEntries = sharedPref.all
        for (entry in allEntries.entries) {
            val valuesList = entry.value.toString().split(",")

            val parent = LinearLayout(this)
            parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)

            parent.orientation = LinearLayout.HORIZONTAL

            for (value in 0 until valuesList.size){
                if (value == 2) {
                    val text = TextView(this)
                    text.text = valuesList.elementAt(value)
                    text.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    parent.addView(text)
                }
            }

            val button = Button(this)
            button.text = "GO"
            button.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            button.setOnClickListener {
                mMap.clear()
                var mp = MarkerOptions()
                mp.position(LatLng(valuesList[0].toDouble(), valuesList[1].toDouble()))
                mp.title("Destination")
                mp.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                destination = mp
                mMap.addMarker(currentLocation)
                mMap.addMarker(destination)
                showBothMarkers()

                distance = valuesList[4].toDouble()
                phone = valuesList[3]
            }
            parent.addView(button)
            listLayout.addView(parent)
        }
    }

    fun registerLocation(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun showBothMarkers(){
        val builder = LatLngBounds.Builder()
        builder.include(currentLocation.position)
        builder.include(destination?.position)
        val bounds = builder.build()

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
    }

}
