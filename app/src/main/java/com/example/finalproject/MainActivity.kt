package com.example.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.finalproject.configuration.API_KEY
import com.example.finalproject.dummy.DummyContent
import com.example.finalproject.network.Api
import com.example.finalproject.network.Service
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class MainActivity : AppCompatActivity() ,
    toDoListFragment.OnListFragmentInteractionListener,
    TareasFragment.OnListFragmentInteractionListener2 {
    var loggedUser = User("test@mail.com","Your","Name","+569 99999999","")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                    var listOfLists:MutableList<MutableList<String>> = ArrayList()
                    val params = listOf("id", "name", "position","created_at","updated_at")
                    for (item in answer){
                        var jsonItem = item as JsonObject
                        var list: MutableList<String> = ArrayList()
                        for (param in params){
                            list.add(jsonItem.get(param).toString())
                        }
                        listOfLists.add(list)
                    }
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.mainContainer,toDoListFragment.newInstance(listOfLists),"TODOLISTS")
                        .commit()
                }
            }
            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
            }
        })

    }

    override fun onListFragmentInteraction(id: String) {
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
                        var listOfLists:MutableList<MutableList<String>> = ArrayList()
                        val params = listOf("id", "name", "position","list_id","starred","done","due_date","notes","created_at","updated_at","lat","long")
                        for (item in answer){
                            var jsonItem = item as JsonObject
                            var list: MutableList<String> = ArrayList()
                            for (param in params){
                                println(jsonItem.get(param))
                                list.add(jsonItem.get(param).toString())
                            }
                            listOfLists.add(list)
                        }
                        supportFragmentManager
                            .beginTransaction()
                            .add(R.id.mainContainer,TareasFragment.newInstance(listOfLists),"TAREASLISTS")
                            .commit()
                    }
                }
                override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                }
            })
        }
    }

    override fun onListFragmentInteraction2(item: String) {
        val intent = Intent(this, ItemDetail::class.java)
        intent.putExtra("ID", item)
        startActivity(intent)
    }

}
data class User(var email: String, var first_name: String, var last_name : String, var phone:String, var profile_photo:String):Serializable

