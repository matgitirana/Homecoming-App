package com.example.homecoming

import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        var address = findViewById<EditText>(R.id.search_address)
        address.setOnFocusChangeListener { _, hasFocus -> searchAddress(address, hasFocus) }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun searchAddress(address: EditText, hasFocus: Boolean){
        if (!hasFocus){
            mMap.clear()
            // colocar  em complement a cidade e país da localização atual
            var complement = ""
            var addressString = "${address.text}, $complement"
            var coder = Geocoder(this)
            var locationList = coder.getFromLocationName(addressString, 1)
            if(locationList.isEmpty()){
                Toast.makeText(this, "Endereço não encontrado, por favor tente novamente", Toast.LENGTH_LONG).show()
            } else {
                var location = locationList.first()

                var point = LatLng(location.latitude, location.longitude)
                mMap.addMarker(MarkerOptions().position(point).title(location.getAddressLine(0)))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0f))
            }



        }

    }


}
