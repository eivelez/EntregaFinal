package com.example.finalproject.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*


interface Api {
    @GET("users/get_self/")
    fun getUser(@Header("token") key: String?): Call<JsonObject>

    @GET("lists/")
    fun getLists(@Header("token")key: String?): Call<JsonArray>


    @GET("items/?")
    fun getListItems(@Query("list_id") listId:String, @Header("token")key:String): Call<JsonArray>

    @GET("items/{id}")
    fun getItem(@Path("id")id:String , @Header("token")key:String): Call<JsonObject>

    @POST("lists/")
    @Headers("Content-Type: application/json")
    fun addList(@Body body: String, @Header("token")key: String): Call<JsonObject>

    @POST("items")
    @Headers("Content-Type: application/json")
    fun addItem(@Body body:String, @Header("token")key:String): Call<JsonArray>

    //@GET("trending")
    //fun getTrend(@Query("api_key") apiKey:String?,@Query("limit") limit:String?):Call<JsonObject>

    //@GET("search")
    //fun getSearch(@Query("api_key") apiKey:String?,@Query("limit")limit:String? ,@Query("q") s:String?):Call<JsonObject>

    //@GET("random")
    //fun getRandom(@Query("api_key") apiKey:String?):Call<JsonObject>

    //@GET("categories")
    //fun getCategories():Call<JsonObject>

    //@GET("images/search")
    //fun getImage(@Header("Authorization") key: String?,@Query("breed_id") breedId: String? ): Call<JsonArray>

    @PUT("update_self")
    @Headers("Content-Type: application/json")
    fun editUser(@Body body: String, @Header("token") key: String?): Call<JsonObject>

}