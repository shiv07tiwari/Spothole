package com.example.wheresthepothole.networking

import com.example.wheresthepothole.objects.Counter
import com.example.wheresthepothole.objects.DataReading
import com.example.wheresthepothole.objects.Pothole
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NetworkService {
    @POST("/ispothole")
    fun checkPothole(@Body potholeQuery: DataReading): Call<String>

    @GET("/userpothole")
    fun getUserPotholes (@Path("user_id") userId : String,
                         @Path("type") type:String) : Call<Pothole>
    @GET("/counter")
    fun getHomeCounters(@Path("city") city : String) : Call<Counter>
}
