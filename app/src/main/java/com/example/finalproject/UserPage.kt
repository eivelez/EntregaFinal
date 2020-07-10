package com.example.finalproject

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.finalproject.configuration.API_KEY
import com.example.finalproject.network.Api
import com.example.finalproject.network.Service
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_user_page.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserPage : AppCompatActivity() {
    var loggedUser = User("test@mail.com","Your","Name","+569 99999999","")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)
        var save = this

        updateUser(save)

        changeUserDataButton.setOnClickListener {
            changeData()
        }

        backButton.setOnClickListener {
            val intent2 = Intent(this,MainActivity::class.java)
            startActivity(intent2)
        }


    }

    fun changeData(){
        val dataJson = JsonObject()
        if(apellidoInputText.text.toString()!=""){
            dataJson.addProperty("last_name", apellidoInputText.text.toString())
            apellidoInputText.hint = apellidoInputText.text.toString()
            apellidoInputText.text.clear()
        }
        if (nameInputText.text.toString()!= ""){
            dataJson.addProperty("first_name",nameInputText.text.toString())
            nameInputText.hint = nameInputText.text.toString()
            nameInputText.text.clear()
        }
        if (phoneInputText.text.toString()!=""){
            dataJson.addProperty("phone",phoneInputText.text.toString())
            phoneInputText.hint = phoneInputText.text.toString()
            phoneInputText.text.clear()
        }


        val request = Service.buildService(Api::class.java)
        val callbla = request.editUser(dataJson.toString(),API_KEY)
        callbla.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })
    }

    fun updateUser(context: Context){
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
                    userMailTextView.text = loggedUser.email.toString()
                    nameInputText.hint = loggedUser.first_name.toString()
                    apellidoInputText.hint = loggedUser.last_name.toString()
                    phoneInputText.hint = loggedUser.phone.toString()
                    Picasso.with(context).load(loggedUser.profile_photo.toString())
                        .resize(100,100)
                        .into(imageView2)
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })
    }
}
