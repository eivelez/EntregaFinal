package com.example.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.finalproject.configuration.API_KEY
import com.example.finalproject.dummy.DummyContent
import com.example.finalproject.network.Api
import com.example.finalproject.network.Service
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
    var listOfLists:MutableList<MutableList<String>> = ArrayList()
    var listOfTareas:MutableList<MutableList<String>> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                supportFragmentManager.beginTransaction()
                    .add(R.id.mainContainer,toDoListFragment.newInstance(listOfLists),"TODOLISTS")
                    .commit()
            }
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
                    var lat:Double = 50.50
                    var long:Double = -85.36
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

}
data class User(var email: String, var first_name: String, var last_name : String, var phone:String, var profile_photo:String):Serializable

