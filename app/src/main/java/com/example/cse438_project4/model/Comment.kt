package com.example.cse438_project4.model

import android.location.Location
import java.io.Serializable

class Comment(): Serializable {


    private var username = ""


    private var body:String=""



    constructor( username:String,body:String): this() {


        this.username=username

        this.body=body
    }
    fun getUsername():String {
        return this.username
    }

    fun getBody(): String{
        return this.body
    }




//    fun getDate(): Timestamp {
//        return date
//    }
}