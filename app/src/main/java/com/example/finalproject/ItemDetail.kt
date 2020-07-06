package com.example.finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.finalproject.configuration.API_KEY
import com.example.finalproject.network.Api
import com.example.finalproject.network.Service
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_item_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemDetail : AppCompatActivity() {

    lateinit var mapFragment : SupportMapFragment
    lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        val id = intent.getStringExtra("ID")
        val request = Service.buildService(Api::class.java)
        val call3 = request.getItem(id, API_KEY)
        call3.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    var answer = response.body() as JsonObject
                    itemTitleTV.text = answer.get("name").asString
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it
        })
    }
}
