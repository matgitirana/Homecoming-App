package com.example.homecoming

import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

            if (isConnectedtoInternet(this)){
                var addressString = address.text.toString()
                if(!addressString.isEmpty()){
                    Log.i("Teste", "oi$addressString 1")
                    var coder = Geocoder(this)
                    Log.i("Teste", "olá")
                    var locationList = coder.getFromLocationName(addressString, 1)
                    Log.i("Teste", locationList.toString())
                    if(locationList.isEmpty()){
                        Toast.makeText(this, "Endereço não encontrado, por favor tente novamente", Toast.LENGTH_LONG).show()
                    } else {
                        var location = locationList.first()

                        var point = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(MarkerOptions().position(point).title(location.getAddressLine(0)))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 10.0f))
                    }
                } else{
                    Toast.makeText(this, "Por favor, digite um endereço", Toast.LENGTH_LONG).show()
                }
            } else{
                Toast.makeText(this, "Por favor, conecte a internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isConnectedtoInternet(context: Context): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

}
