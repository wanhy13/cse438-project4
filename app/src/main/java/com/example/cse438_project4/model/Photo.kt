package com.example.cse438_project4.model

import android.location.Location
import java.io.Serializable

class Photo(): Serializable {

    private var location: Location = Location("");
    private var username = ""
    private var color = ""

    private var date:String=""
    private var currentPath=""


    constructor( location:Location, date: String, currentPath:String): this() {

        this.location = location
        this.color=color
        this.username=username
        this.date = date
        this.currentPath=currentPath
    }



    fun getLocation(): Location {
        return this.location
    }
    fun getDate():String {
        return this.date
    }

    fun getCurrentPath(): String{
        return this.currentPath
    }

//    fun getDate(): Timestamp {
//        return date
//    }
}