package com.example.homecoming

import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class RegisterActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var addressLatLng: LatLng
    private var preferenceKey = 0
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        var addressInput = findViewById<EditText>(R.id.search_address)
        addressInput.setOnFocusChangeListener { _, hasFocus -> searchAddress(addressInput, hasFocus) }
        sharedPref = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        for (entry in sharedPref.all.entries) {
            preferenceKey++
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun searchAddress(addressText: EditText, hasFocus: Boolean){
        if (!hasFocus){
            mMap.clear()

            if (isConnectedToInternet(this)){
                var addressString = addressText.text.toString()

                if(addressString.isNotEmpty()){
                    var coder = Geocoder(this)
                    var locationList = coder.getFromLocationName(addressString, 1)

                    if(locationList.isEmpty()){
                        Toast.makeText(this, "Address not found, try again", Toast.LENGTH_LONG).show()

                    } else {
                        var location = locationList.first()
                        addressLatLng = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(MarkerOptions().position(addressLatLng).title(location.getAddressLine(0)))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(addressLatLng, 16.0f))

                    }
                } else{
                    Toast.makeText(this, "Type an address", Toast.LENGTH_LONG).show()
                }
            } else{
                Toast.makeText(this, "Connect to the internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isConnectedToInternet(context: Context): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    fun registerData(v: View){
        var addressInput = findViewById<EditText>(R.id.search_address)
        addressInput.clearFocus()

        var tag = findViewById<EditText>(R.id.tag).text.toString()
        var phone = findViewById<EditText>(R.id.phone).text.toString()
        var distance = findViewById<EditText>(R.id.distance).text.toString()

        if(::addressLatLng.isInitialized && tag.isNotEmpty() && phone.isNotEmpty() && distance.isNotEmpty()){
            var addressLat = addressLatLng.latitude.toString()
            var addressLng = addressLatLng.longitude.toString()
            var stringData = "$addressLat,$addressLng,$tag,$phone,$distance"

            val editor = sharedPref.edit()
            editor.putString(preferenceKey.toString(), stringData)
            editor.commit()

            preferenceKey++

            finish()
        } else {
            Toast.makeText(this, "Fill all fields correctly", Toast.LENGTH_LONG).show()
        }


    }

}
