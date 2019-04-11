package com.example.cse438_project4

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.detail_activity.*
import com.example.cse438_project4.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.dialog.view.*


class DetailActivity : AppCompatActivity(){

    private var username=""
    private var currentPath=""
    private var date=""
    private var lat=""
    private var long=""
    private lateinit var mDetector: GestureDetectorCompat
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_activity)

        mDetector = GestureDetectorCompat(this, MyGestureListener())


        backButton.setOnClickListener (){
            //Log.e("playbutton","work")
            val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)
        }

        username=intent.getStringExtra("USERNAME");
        date=intent.getStringExtra("DATE");
        lat=intent.getStringExtra("LAT");
        long=intent.getStringExtra("LONG");
        currentPath = intent.getStringExtra("CURRENTPATH");
        var formatofdate =date.split("_")
        textView.text="Uername: "+username
        textView2.text="Date: "+formatofdate[0]
        textView3.text="Position.lat = "+lat+"  Position.long = "+long

        App.firebaseAuth = FirebaseAuth.getInstance()
        val storageReference = FirebaseStorage.getInstance().getReference().child(currentPath);
        val image = findViewById<ImageView>(R.id.imageView)
//        //val downloadUri =
        Glide.with(this /* context */)
            .load(currentPath)
            .into(imageView)
        comment.setOnClickListener(){
            val intent = Intent(this,CommentActivity::class.java)
            intent.putExtra("USER",username)
            intent.putExtra("PATH",currentPath)

            intent.putExtra("DATE",date)
            intent.putExtra("LAT", lat)
            intent.putExtra("LONG",long)


            startActivity(intent)
        }
        add.setOnClickListener(){
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Comment")
            val mAlertDialog = mBuilder.show()
            mDialogView.submit.setOnClickListener({
                mAlertDialog.dismiss()
                var commentBody = mDialogView.yourgoal.text.toString()
                if(commentBody==null||commentBody==""){
                    val toast=Toast.makeText(applicationContext,"Comments can't be null!",Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)

                    toast.show()

                }else {
                    val userId = App.firebaseAuth?.currentUser?.uid

                    val db = FirebaseFirestore.getInstance()

                    var currentusername = ""

                    db.collection("users").document(userId!!).get().addOnCompleteListener { it2 ->
                        if (it2.isSuccessful) {
                            val userData = it2.result!!
                            currentusername = userData.get("username") as String
                            Log.e("username:", currentusername)
                            var n = currentPath.split("/")
                            var p = n[n.size - 1]
                            Log.e("p", p)

                            db.collection("comment").document(p).get().addOnCompleteListener { it ->
                                if (it.isSuccessful) {

                                    val userData2 = it.result!!
                                    Log.e("userData", userData2.toString())

                                    var data = userData2.get("comment") as? ArrayList<Comment>
                                    Log.e("data in commet", data.toString())
                                    if (data == null) {
                                        data = ArrayList<Comment>()
                                    }

                                    data.add(Comment(currentusername, commentBody))
                                    val map = hashMapOf(
                                        Pair("photo", p),
                                        Pair("comment", data)
                                    )
                                    db.collection("comment").document(p).set(map)
                                    val toast=Toast.makeText(applicationContext,"Comments add successfully! See more on comment page!",Toast.LENGTH_SHORT)
                                    toast.setGravity(Gravity.CENTER, 0, 0)

                                    toast.show()

                                } else {
                                    Toast.makeText(this, "Unable to get score", Toast.LENGTH_SHORT).show()
                                }
                            }

                        } else {
                            Toast.makeText(this, "Unable to get score", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })

        }


    }
    private fun startMap(){
        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)
    }
    private fun startComment(){
        val intent = Intent(this,CommentActivity::class.java)
        intent.putExtra("USER",username)
        intent.putExtra("PATH",currentPath)

        intent.putExtra("DATE",date)
        intent.putExtra("LAT", lat)
        intent.putExtra("LONG",long)


        startActivity(intent)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if(gestureworks) {
        mDetector.onTouchEvent(event)
        // if we don't have event to handle, it will handle by default in system
        return super.onTouchEvent(event)
//        }
//        return super.onTouchEvent(event)
    }
    private fun add(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Comment")
        val mAlertDialog = mBuilder.show()
        mDialogView.submit.setOnClickListener({
            mAlertDialog.dismiss()
            var commentBody = mDialogView.yourgoal.text.toString()

            val userId = App.firebaseAuth?.currentUser?.uid

            val db = FirebaseFirestore.getInstance()

            var currentusername=""

            db.collection("users").document(userId!!).get().addOnCompleteListener { it2 ->
                if (it2.isSuccessful) {
                    val userData = it2.result!!
                    currentusername = userData.get("username") as String
                    Log.e("username:",currentusername)
                    var n = currentPath.split("/")
                    var p=n[n.size-1]
                    Log.e("p",p)

                    db.collection("comment").document(p).get().addOnCompleteListener { it->
                        if(it.isSuccessful){

                            val userData2 = it.result!!
                            Log.e("userData",userData2.toString())

                            var data=userData2.get("comment") as? ArrayList<Comment>
                            Log.e("data in commet",data.toString())
                            if(data==null){
                                data=ArrayList<Comment>()
                            }

                            data.add(Comment(currentusername,commentBody))
                            val map = hashMapOf(
                                Pair("photo", p),
                                Pair("comment", data)
                            )
                            db.collection("comment").document(p).set(map)

                        }
                    }

                }
            }

        })
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        private var swipedistance = 150

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            //doubletap is stand
            Log.e("double ", "double d")


            add()
                return true

        }

        //e1 start e2 end
        // swipedistance
        //player's turn, right swipe, hit
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e2.x - e1.x > swipedistance) {
                   // Log.e("right", "to right")
                    startMap()

                    return true
                }


                if (e1.x - e2.x > swipedistance) {
                    //Log.e("left", "to left")

                    startComment()
                    return true

                }

            return false
        }
    }
}