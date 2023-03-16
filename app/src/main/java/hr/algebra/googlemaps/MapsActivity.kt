package hr.algebra.googlemaps

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import hr.algebra.googlemaps.databinding.ActivityMapsBinding
import java.util.*


private val LAT_LNG_ZG = LatLng( 45.817,16.0 )
private val LAT_LNG_ST = LatLng( 43.508133, 16.440193 )
private val LOCATION_PERMISSION_REQUEST = 698

class MapsActivity : AppCompatActivity( ), OnMapReadyCallback {

    private val TAG = "MapsActivity"
    private lateinit var mMap                : GoogleMap
    private lateinit var binding             : ActivityMapsBinding
    private lateinit var fusedLocationClient : FusedLocationProviderClient


    override fun onCreate( savedInstanceState: Bundle? ) {
        super.onCreate( savedInstanceState )


        Geocoder.isPresent()

        binding = ActivityMapsBinding.inflate( layoutInflater )
        setContentView( binding.root )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient( this )



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById( R.id.map ) as SupportMapFragment
        mapFragment.getMapAsync( this )
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady( googleMap: GoogleMap ) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        // val sydney = LatLng( -34.0, 151.0 )
        mMap.addMarker( MarkerOptions( ).position( LAT_LNG_ZG ).title( "Marker in Zagreb" ) )
        mMap.moveCamera( CameraUpdateFactory.newLatLng( LAT_LNG_ZG ) )

        mMap.uiSettings.isZoomControlsEnabled      = true
        mMap.uiSettings.isCompassEnabled           = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isMapToolbarEnabled        = true

        mMap.setOnMapClickListener {
            mMap.addMarker( MarkerOptions( ).position( it ) )
            getAddressFromLatLng( it )
        }

        if( !checkPermissions( ) )
            requestPermissions( )
        pretplatiSeNaLokaciju( )
        Toast
            .makeText( this, "Geocoder is present: ${Geocoder.isPresent()}", Toast.LENGTH_SHORT )
            .show( )
        Thread{
            val coord = getLatLngFromAddress( "Zagreb" )
            runOnUiThread {
                Toast
                    .makeText( this, "$coord", Toast.LENGTH_SHORT )
                    .show( )
            }
        }.start( )

    }

    private fun readAddressFromGeocoder( addresses : List< Address >? ) {
        Handler( mainLooper )
            .post {
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val sb = StringBuilder("")
                    for (i in 0 until address.maxAddressLineIndex)
                        sb.append(address.getAddressLine(i))
                            .append("\n")
                    Toast
                        .makeText(this, sb.toString(), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast
                        .makeText(this, "Nothing is here", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    @SuppressLint( "MissingPermission" )
    private fun pretplatiSeNaLokaciju( ) {
        if( checkPermissions( ) )
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.Builder( 10000 ).build( ),
                {
                    Toast
                        .makeText( this, "Lokacija se promijenila. Sada je: $it", Toast.LENGTH_SHORT )
                        .show( )
                },
                Looper.myLooper( )
            )
        else
            requestPermissions( )
    }

    private fun getLatLngFromAddress( addressText : String ) {
        val geocoder = Geocoder( this, Locale.ENGLISH )
        val addresses = geocoder.getFromLocationName( addressText, 1 )
        if( addresses!=null ) {
            val address = addresses[0]
            Toast
                .makeText(this, "$addressText - (${address.latitude}, ${address.longitude})", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getAddressFromLatLng( latLng : LatLng ) {

        try {
            Log.i( TAG, "Getting the Geocoder" )
            val geocoder = Geocoder( this, Locale.ENGLISH )
            Log.i( TAG, "Geocoder available" )

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
                Log.i( TAG, "Geocoding for Tiramisu" )
                geocoder.getFromLocation( latLng.latitude, latLng.longitude, 1, ::readAddressFromGeocoder )
            } else {
                Log.i( TAG, "Geocoding for older version" )
                readAddressFromGeocoder( geocoder.getFromLocation( latLng.latitude, latLng.longitude, 1 ) )
            }

        } catch ( e : Exception ) {
            e.message?.let { Log.e( TAG, it ) }
        }
    }

    override fun onCreateOptionsMenu( menu: Menu? ): Boolean {
        menuInflater.inflate( R.menu.menu, menu )
        return true
    }

    @SuppressLint( "MissingPermission" )
    override fun onOptionsItemSelected(item : MenuItem): Boolean {
        if( item.itemId==R.id.bZagreb ) {
//          Toast.makeText( this, "Evo me!!", Toast.LENGTH_SHORT ).show( )
            //mMap.moveCamera( CameraUpdateFactory.newLatLngZoom( LAT_LNG_ZG, 2f ) );
            mMap.animateCamera( CameraUpdateFactory.zoomTo( 18f ), 2000, null );
//          getLatLngFromAddress( "London" )
        } else if( item.itemId==R.id.bSplit ) {
            val cameraPosition = CameraPosition.Builder()
                                    .target( LAT_LNG_ST ) // Sets the center of the map to Mountain View
                                    .zoom( 17f )          // Sets the zoom
                                    .bearing( 90f )       // Sets the orientation of the camera to east
                                    .tilt( 30f )          // Sets the tilt of the camera to 30 degrees
                                    .build( )
            mMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ), 5000, null );

        } else if( item.itemId==R.id.bCurrentLocation ) {
            if( checkPermissions( ) )
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        Toast
                            .makeText( this, "Your location is: $location", Toast.LENGTH_SHORT )
                            .show( )
                    }
            else
                requestPermissions( )

        }
        return true
    }

    private fun checkPermissions( ) : Boolean {
        return ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions( ) {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), LOCATION_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array< out String >,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults )
        if( requestCode == LOCATION_PERMISSION_REQUEST ) {
            if( grantResults.isNotEmpty( ) && grantResults[0]==PackageManager.PERMISSION_GRANTED )
                Toast.makeText( this, "Hvala Vam na povjerenju", Toast.LENGTH_SHORT ).show( )
        }
    }

}