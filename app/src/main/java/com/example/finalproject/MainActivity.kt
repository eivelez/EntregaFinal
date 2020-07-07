package com.example.finalproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.finalproject.configuration.API_KEY
import com.example.finalproject.dummy.DummyContent
import com.example.finalproject.network.Api
import com.example.finalproject.network.Service
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.new_list_dialog.*
import kotlinx.android.synthetic.main.new_list_dialog.view.*
import kotlinx.android.synthetic.main.new_list_dialog.view.newListInput
import kotlinx.android.synthetic.main.new_tarea_dialog.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class MainActivity : AppCompatActivity() ,
    toDoListFragment.OnListFragmentInteractionListener,
    TareasFragment.OnListFragmentInteractionListener2 {
    var loggedUser = User("test@mail.com","Your","Name","+569 99999999","")
    var buttonState = "list"
    var listCant = 0
    var openList = 0
    var tareaCant = 0
    var lastlat:Double = 0.0
    var lastlong:Double = 0.0
    var listOfLists:MutableList<MutableList<String>> = ArrayList()
    var listOfTareas:MutableList<MutableList<String>> = ArrayList()
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var PERMISSION_ID = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonLogout.setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    // ...
                }
            super.onBackPressed()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        buttonCreate.text = "+ Nueva lista"
        buttonCreate.setOnClickListener {
            addElement()
        }
        val request = Service.buildService(Api::class.java)
        val call1 = request.getUser(API_KEY)
        call1.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    var answer = response.body() as JsonObject
                    loggedUser.email = answer.get("email").asString
                    loggedUser.first_name = answer.get("first_name").asString
                    loggedUser.last_name = answer.get("last_name").asString
                    loggedUser.phone = answer.get("phone").asString
                    loggedUser.profile_photo = answer.get("profile_photo").asString
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })

        val call2 = request.getLists(API_KEY)
        call2.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                if (response.isSuccessful) {
                    var answer = response.body() as JsonArray
                    listCant = answer.size()
                    listOfLists = ArrayList()
                    val params = listOf("id", "name", "position","created_at","updated_at")
                    for (item in answer){
                        var jsonItem = item as JsonObject
                        var list: MutableList<String> = ArrayList()
                        for (param in params){
                            list.add(jsonItem.get(param).toString())
                        }
                        listOfLists.add(list)
                    }
                    val callSL = request.getSharedLists(API_KEY)
                    callSL.enqueue(object : Callback<JsonArray> {
                        override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                            if (response.isSuccessful) {
                                var answer = response.body() as JsonArray
                                var list: MutableList<String> = ArrayList()
                                for (item in answer){
                                    var jsonItem = item as JsonObject
                                    list.add(jsonItem.get("list_id").toString())
                                }
                                for (sl in list){
                                    val callSLDetail = request.getList(sl,API_KEY)
                                    callSLDetail.enqueue(object : Callback<JsonObject> {
                                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                                            if (response.isSuccessful) {
                                                var answer = response.body() as JsonObject
                                                var stringAux=answer.get("name").asString
                                                stringAux+=" (compartida)"
                                                val mList: MutableList<String> = ArrayList()
                                                val aux = listOf(answer.get("id").toString(),
                                                    stringAux.toString(),
                                                    answer.get("position").toString(),
                                                    answer.get("created_at").toString(),
                                                    answer.get("updated_at").toString())
                                                for (i in aux){
                                                    mList.add(i)
                                                }
                                                listOfLists.add(mList)
                                                if(sl==list[list.size-1]){
                                                    supportFragmentManager
                                                        .beginTransaction()
                                                        .add(R.id.mainContainer,toDoListFragment.newInstance(listOfLists),"TODOLISTS")
                                                        .commit()
                                                }
                                            }
                                        }
                                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                        }
                                    })
                                }
                            }
                        }
                        override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                        }
                    })
                }
            }
            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
            }
        })

    }

    override fun onListFragmentInteraction(id: String) {
        openList=id.toInt()
        var openFragment = supportFragmentManager.findFragmentByTag("TODOLISTS")
        if (openFragment != null) {
            supportFragmentManager
                .beginTransaction()
                .remove(openFragment)
                .commit()
            val request = Service.buildService(Api::class.java)
            val call3 = request.getListItems(id,API_KEY)
            call3.enqueue(object : Callback<JsonArray> {
                override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                    if (response.isSuccessful) {
                        var answer = response.body() as JsonArray
                        listOfTareas = ArrayList()
                        val params = listOf("id", "name", "position","list_id","starred","done","due_date","notes","created_at","updated_at","lat","long")
                        tareaCant = answer.size()
                        for (item in answer){
                            var jsonItem = item as JsonObject
                            var list: MutableList<String> = ArrayList()
                            for (param in params){
                                list.add(jsonItem.get(param).toString())
                            }
                            listOfTareas.add(list)
                        }
                        supportFragmentManager
                            .beginTransaction()
                            .add(R.id.mainContainer,TareasFragment.newInstance(listOfTareas),"TAREASLISTS")
                            .commit()
                        buttonCreate.text = "+ Nueva tarea"
                        buttonState = "tarea"
                    }
                }
                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                }
            })
        }
    }

    override fun onBackPressed() {
        if (buttonState=="tarea"){

            var openFragment = supportFragmentManager.findFragmentByTag("TAREASLISTS")
            if (openFragment != null) {
                supportFragmentManager.beginTransaction().remove(openFragment).commit()
                buttonState = "list"
                buttonCreate.text = "+ Nueva Lista"
                supportFragmentManager.beginTransaction()
                    .add(R.id.mainContainer,toDoListFragment.newInstance(listOfLists),"TODOLISTS")
                    .commit()
            }
        }
        else if (buttonState=="list"){
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    // ...
                }
            super.onBackPressed()
        }

    }

    override fun onListFragmentInteraction2(item: String) {
        val intent = Intent(this, ItemDetail::class.java)
        intent.putExtra("ID", item)
        startActivity(intent)
    }

    fun addElement(){
        if (buttonState == "list"){
            val mDialogView =  LayoutInflater.from(this).inflate(R.layout.new_list_dialog,null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Nombre nueva lista:")
            val mAlertDialog = mBuilder.show()
            mDialogView.createNewListButton.setOnClickListener{
                val dialogInput = mDialogView.newListInput.text.toString()
                if (dialogInput!=""){
                    mAlertDialog.dismiss()
                    val dataJson = JsonObject()
                    dataJson.addProperty("position",listCant)
                    dataJson.addProperty("name",dialogInput)

                    val request = Service.buildService(Api::class.java)
                    val call4 = request.addList(dataJson.toString(),API_KEY)
                    call4.enqueue(object : Callback<JsonObject> {
                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            if (response.isSuccessful) {
                                var responseJ = response.body() as JsonObject
                                val newListData:MutableList<String> = ArrayList()

                                val aux = listOf(responseJ.get("id").toString(),
                                    responseJ.get("name").toString(),
                                    responseJ.get("position").toString(),
                                    responseJ.get("created_at").toString(),
                                    responseJ.get("updated_at").toString())

                                for (i in aux){
                                    newListData.add(i)
                                }

                                listOfLists.add(newListData)
                                var openFragment = supportFragmentManager.findFragmentByTag("TODOLISTS")
                                if (openFragment != null) {
                                    supportFragmentManager
                                        .beginTransaction()
                                        .remove(openFragment).commit()
                                    supportFragmentManager
                                        .beginTransaction()
                                        .add(R.id.mainContainer,toDoListFragment.newInstance(listOfLists),"TODOLISTS")
                                        .commit()
                                }
                            }
                        }
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        }
                    })
                }
            }
            mDialogView.cancelNewListButton.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
        else if (buttonState == "tarea"){
            val mDialogView =  LayoutInflater.from(this).inflate(R.layout.new_tarea_dialog,null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Nombre nueva tarea:")
            val mAlertDialog = mBuilder.show()
            mDialogView.createNewTareaButton.setOnClickListener{
                val dialogInput = mDialogView.newTareaInput.text.toString()
                if (dialogInput!=""){
                    mAlertDialog.dismiss()
                    val dataJson = JsonObject()
                    val jsonMain = JsonObject()


                    val itemContainer = JsonArray()
                    dataJson.addProperty("position",tareaCant)
                    dataJson.addProperty("name",dialogInput)
                    dataJson.addProperty("list_id",openList)
                    var dateSample = "2022-06-15 23:59:59"
                    dataJson.addProperty("due_date",dateSample)
                    dataJson.addProperty("starred",false)
                    var noteSample = "Esta nota es generada como muestra para inluirla en los items creados, lorem ipsum bla bla bla"
                    dataJson.addProperty("notes",noteSample)
                    var lat:Double = lastlat
                    var long:Double = lastlong
                    dataJson.addProperty("lat",lat)
                    dataJson.addProperty("long",long)
                    //println(dataJson)

                    itemContainer.add(dataJson)
                    //println(itemContainer)

                    jsonMain.add ("items",itemContainer)

                    //println(jsonMain.toString())
                    val request = Service.buildService(Api::class.java)
                    val call5 = request.addItem(jsonMain.toString(),API_KEY)
                    call5.enqueue(object : Callback<JsonArray> {
                        override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                            if (response.isSuccessful) {
                                var responseJA = response.body() as JsonArray
                                var responseJ = responseJA[0] as JsonObject
                                val newTareaData:MutableList<String> = ArrayList()
                                val aux = listOf(responseJ.get("id").toString(),
                                    responseJ.get("name").toString(),
                                    responseJ.get("position").toString(),
                                    responseJ.get("list_id").toString(),
                                    responseJ.get("starred").toString(),
                                    responseJ.get("done").toString(),
                                    responseJ.get("due_date").toString(),
                                    responseJ.get("notes").toString(),
                                    responseJ.get("created_at").toString(),
                                    responseJ.get("updated_at").toString(),
                                    responseJ.get("lat").toString(),
                                    responseJ.get("long").toString())

                                for (i in aux){
                                    newTareaData.add(i)
                                }

                                listOfTareas.add(newTareaData)
                                var openFragment = supportFragmentManager.findFragmentByTag("TAREASLISTS")
                                if (openFragment != null) {
                                    supportFragmentManager
                                        .beginTransaction()
                                        .remove(openFragment)
                                        .commit()
                                    supportFragmentManager
                                        .beginTransaction()
                                        .add(R.id.mainContainer,TareasFragment.newInstance(listOfTareas),"TAREASLISTS")
                                        .commit()
                                }
                            }
                        }
                        override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                        }
                    })
                }
            }
            mDialogView.cancelNewTareaButton.setOnClickListener {
                mAlertDialog.dismiss()
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        requestNewLocationData()
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        if (location != null) {
                            lastlat = location.latitude
                            lastlong = location.longitude

                        }
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            lastlat = mLastLocation.latitude
            lastlong = mLastLocation.longitude
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }




}




data class User(var email: String, var first_name: String, var last_name : String, var phone:String, var profile_photo:String):Serializable

