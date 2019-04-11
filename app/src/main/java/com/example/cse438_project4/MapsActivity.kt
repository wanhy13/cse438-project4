package com.example.cse438_project4


import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.cse438_project4.model.Photo
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.common.collect.MapMaker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var mMap: GoogleMap
    private var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    private var REQUEST_CODE = 1001
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest
    private lateinit var locationCallback : LocationCallback
    private lateinit var lastLocation: Location
    private var locationUpdateState = false


    //Image

    private var currentPhotoPath: String = ""
    private var REQUEST_IMAGE_CAPTURE: Int = 1
    private var REQUEST_LOAD_IMAGE: Int = 2
    private lateinit var photoURI:Uri
    private var date=""
    private lateinit var locationlist : ArrayList<ArrayList<Double>>
    private lateinit var informationlist: ArrayList<ArrayList<String>>


    private var storage = FirebaseStorage.getInstance()
    // Create a storage reference from our app
    // Create a storage reference from our app
    private var storageRef = storage.reference
    private lateinit var mProgress :ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
        map.onCreate(savedInstanceState)
        map.onResume()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo == null) {
            Log.e("NETWORK", "not connected")
        }

        else {
            Log.e("NETWORK", "connected")
            if (App.firebaseAuth == null) {
                App.firebaseAuth = FirebaseAuth.getInstance()
                //Log.e("main2",FirebaseAuth.getInstance().toString())
                //Log.e("main3",App.firebaseAuth?.currentUser.toString())
            }

            if (App.firebaseAuth != null && App.firebaseAuth?.currentUser == null) {
                //Log.e("main","2")
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)

            }
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        camera_button.setOnClickListener(this)
        logout_button.setOnClickListener(){
            App.firebaseAuth?.signOut()
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }


        mProgress = ProgressDialog(this);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        map.getMapAsync {
            mMap = it
            setUpMap()
            uploadLocation()
            //Log.e("locationlist",locationlist.toString())
        }

        createLocationRequest()

        locationCallback = object :LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation

            }
        }
        uploadLocation()
    }

    override fun onStart() {
        super.onStart()
        uploadLocation()
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
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//    }
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.camera_button -> {
                // what action we want to do
                // is the image capture
                // also: chain statements together we will run it after the first complete
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // go find the in your  phone what you can do in your activity
                    //package manager is a list of activities that exist on the phone
                    takePictureIntent.resolveActivity(packageManager).also {
                        //try to create a file
                        // whether we have the permission to create a file
                        // we use try and catch
                        //? we can made the file be null
                        val photoFile : File? = try {
                            createImageFile()
                        } catch (ex: IOException) {
                            Log.d("ERROR:", "Could not get photo file")
                            null
                        }
                        // if photo file exist
                        // not null also
                        // start the activit, make file provider if we can access the file
                        photoFile?.also {

                             photoURI = FileProvider.getUriForFile(
                                this,
                                "com.example.cse438_project4.fileprovider",
                                it
                            )
                            //what is the output we want to store
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }
                    }

                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if(!locationUpdateState) {
            startLocationUpdates()
        }
        //to make sure code doesn't run in background, then you could notice the position change
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationUpdateState = false
        this.fusedLocationClient.removeLocationUpdates(locationCallback)
        //to make sure code doesn't run in background
        map.onPause()
    }

    private fun startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        this.fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_CODE) {
            if(grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }

    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()

        locationRequest.interval = 10000

        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        var builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
    }

    private fun setUpMap(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        this.fusedLocationClient.lastLocation.addOnSuccessListener {
            lastLocation = it
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lastLocation.latitude,lastLocation.longitude)))
        }
    }

    private fun placeMarkerOnMap(lastLocation: LatLng,index:Int){
        var mark:MarkerOptions = MarkerOptions()
        var colornum = informationlist.get(index).get(2);
        var color=(colornum.toInt()*17)%360

        mark.position(LatLng(lastLocation.latitude, lastLocation.longitude))
            .title(index.toString())
            .icon(BitmapDescriptorFactory.defaultMarker(color.toFloat()))
        mMap.addMarker(mark)
        mMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
//            val venueID = mMarkerMap.get(marker.id)
//            val venueName = marker.title
//            val intent = Intent(this@MapActivity, NewActivity::class.java)
//            intent.putExtra(VENUE_NAME, venueName)
//            intent.putExtra(VENUE_ID, venueID)
//            ContextCompat.startActivity(intent)
            val intent = Intent(this,DetailActivity::class.java)
            var index = marker.title.toInt();
            startActivity(intent)
            intent.putExtra("USERNAME",informationlist.get(index).get(0))
            intent.putExtra("DATE",informationlist.get(index).get(1))
            intent.putExtra("LAT", locationlist.get(index).get(0).toString())
            intent.putExtra("LONG",locationlist.get(index).get(1).toString())
            intent.putExtra("CURRENTPATH",informationlist.get(index).get(3))
            startActivity(intent)
//            Log.e("marker:",marker.position.toString());
//            Log.e("index:",index.toString());
//            Log.e("USERNAME",informationlist.get(index).get(0))
//            Log.e("DATE",informationlist.get(index).get(1))
//            Log.e("LAT", locationlist.get(index).get(0).toString())
//            Log.e("LONG",locationlist.get(index).get(1).toString())
//            Log.e("CURRENTPATH",informationlist.get(index).get(3))

            false
        })
    }
    private fun onMarkerClick(marker: Marker){

    }

    //Image

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        date = timeStamp
        // find the dir that have pic
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //createTempfile ( file name , extension, where to store)
        return File.createTempFile(
            "JPEG_$timeStamp",
            ".jpg",
            storageDir
        ).apply {
            // remember the path
            // apply we don't need the it.
            currentPhotoPath = absolutePath
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic(data)
        }
    }

    private fun galleryAddPic(data:Intent?) {
        mProgress.setMessage("Uploading file.....")
        mProgress.show()
        App.firebaseAuth = FirebaseAuth.getInstance()
        var uid=App.firebaseAuth!!.currentUser!!.uid

// Create a reference to "mountains.jpg"
        var ref = storageRef.child("/"+uid+"/"+currentPhotoPath)

        ref.putFile(photoURI)
            .addOnSuccessListener {
                mProgress.dismiss();
                Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                updatePhoto(this)

            }
            .addOnFailureListener {
                mProgress.dismiss();
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
            }



    }
    fun updatePhoto(context:Context){
        val userId = App.firebaseAuth?.currentUser?.uid
        Log.e("Uri",photoURI.toString())
        if(userId!=null)
        {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(App.firebaseAuth!!.currentUser!!.uid).get().addOnCompleteListener { it1 ->
                if (it1.isSuccessful) {
                    val userData = it1.result!!
                    Log.e("userData in photo",userData.toString())
                    Log.e("doc",App.firebaseAuth!!.currentUser!!.uid)


                    var data1 = userData.get("photo") as? ArrayList<Photo>
                    Log.e("data1 in photo", data1.toString());

                    if(data1==null){
                        data1=ArrayList<Photo>()
                    }
                    var photo = Photo(lastLocation,date,photoURI.toString())
                    data1.add(photo)


                    val map2 = hashMapOf(
                        Pair("username", userData.get("username")),
                        Pair("userId", userId),
                        Pair("photo", data1)
                    )



                    db.collection("users").document(App.firebaseAuth!!.currentUser!!.uid).set(map2)
                    uploadLocation()
                }
            }
        }
        Log.e("update photo","work first!")
    }
    //upload the location
    private fun uploadLocation(){
        informationlist=ArrayList<ArrayList<String>>()
        locationlist=ArrayList<ArrayList<Double>>()
        if(App.firebaseAuth?.currentUser == null){
            Log.e("Error","currentUser is null" )
        }
        else {

                    val userId = App.firebaseAuth?.currentUser?.uid

                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").get().addOnCompleteListener { it2 ->
                        //Log.e("it2", it2.result.toString())
                        if(it2.isSuccessful){
                            val usersData = it2.result!!
                            var numOfColor=1;


                            for (i in usersData){
                                var information = ArrayList<String>()
                                var username = i.get("username") as String
                                var photolist = i.get("photo") as? ArrayList<HashMap<String,Any>>
                                var num = usersData.indexOf(i)
                                // Log.e("photolist",photolist.toString())
                                if(photolist!=null){
                                    for (l in photolist){
                                        var num2 = photolist.indexOf(l)
                                        if(l!=null){

                                            var loc = l.get("location") as HashMap<String,Any>

                                            var lat = loc.get("latitude") as Double
                                            var long = loc.get("longitude") as Double
                                            var date=l.get("date") as String
                                            var path = l.get("currentPath") as String
                                            var hash = ArrayList<Double>()
                                            hash.add(lat)
                                            hash.add(long)
                                            locationlist.add(hash)
                                            information.add(username)
                                            information.add(date)
                                            information.add(numOfColor.toString())
                                            information.add(path)
                                            informationlist.add(information)
                                            Log.e("uploadlocation",locationlist.toString());

                                        }
//                                        if(num==usersData.size()-1&&num2==photolist.size-1){
//                                            Log.e("uploadlocation2","work last");
//                                            addMarker()
//                                            Log.e("locationlist",locationlist.toString())
//                                        }

                                    }

                                }
                                numOfColor++;



                            }
                            runOnUiThread(){
                                addMarker()
                            }
                        }



            }
        }


    }
    private fun addMarker(){
        mMap.clear()
        for (i in locationlist){
            placeMarkerOnMap(LatLng(i.get(0),i.get(1)),locationlist.indexOf(i))
        }
    }

}
