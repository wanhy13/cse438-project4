package com.example.cse438_project4

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.example.cse438_project4.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    private var username=""
    private var currentPath=""
    private var date=""
    private var lat=""
    private var long=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        var foodCalorieList: ArrayList<String> = ArrayList()



         username=intent.getStringExtra("USER");
         currentPath = intent.getStringExtra("PATH");
         date=intent.getStringExtra("DATE");
         lat=intent.getStringExtra("LAT");
         long=intent.getStringExtra("LONG");

        var h = currentPath.split("/")
        var p = h[h.size-1]
        detailButton.setOnClickListener(){
            val intent = Intent(this,DetailActivity::class.java)
            intent.putExtra("USERNAME",username)
            intent.putExtra("DATE",date)
            intent.putExtra("LAT", lat)
            intent.putExtra("LONG",long)
            intent.putExtra("CURRENTPATH",currentPath)
            startActivity(intent)
        }
        val db = FirebaseFirestore.getInstance()
        var hash = ArrayList<ArrayList<String>>()
            db.collection("comment").document(p).get().addOnCompleteListener { it->
            if(it.isSuccessful){

                val userData = it.result!!

                var data=userData.get("comment") as? ArrayList<HashMap<String,String>>
                if(data!=null){
                    for(i in 0..data.size-1){
                        var ha= ArrayList<String>()
                        ha.add(data[i].get("username") as String)
                        ha.add(data[i].get("body") as String)
                        hash.add(ha)
                    }
                }
                runOnUiThread(){

                    for(i in 0..hash.size-1){
                        //Log.e("Error", "Layout working ?" )
                        foodCalorieList.add(hash.get(i).get(0)+":  "+hash.get(i).get(1))
                        val adapter = ArrayAdapter(this, R.layout.list, foodCalorieList)
                        var listView= this.findViewById<ListView>(R.id.foodList)
                        listView.setAdapter(adapter)
                    }
                }

            }else{
                Toast.makeText(this,"Unable to get score", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
