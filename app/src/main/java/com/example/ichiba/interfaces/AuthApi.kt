package com.example.ichiba.interfaces

import com.example.ichiba.dataclass.AuthResponse
import retrofit2.Call
import com.example.ichiba.dataclass.LoginRequest
import com.example.ichiba.dataclass.UpdatePhone
import com.example.ichiba.dataclass.UpdateProfile
import com.example.ichiba.dataclass.UpdateUPI
import com.example.ichiba.dataclass.UpdateUserRequest
import com.example.ichiba.dataclass.User
import com.example.ichiba.dataclass.VerifyRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {
    @Headers("Accept: application/json")
    @POST("api/v1/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("api/v1/auth/verify/{id}")
    fun verify(
        @Header("Authorization") accessToken: String,
        @Body request: VerifyRequest,
        @Path("id") userId: String,
    ): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @GET("api/v1/user/profile")
    fun getUser(
        @Header("Authorization") accessToken: String,
        @Path("id") userId: String
    ): Call<User>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("api/v1/user/updateProfile/{id}")
    fun updateProfilePicture(
        @Header("Authorization") accessToken: String,
        @Path("id") userId: String,
        @Body updateProfile: UpdateProfile
    ): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("api/v1/user/updateProfile/{id}")
    fun updatePhoneNumber(
        @Header("Authorization") accessToken: String,
        @Path("id") userId: String,
        @Body userPhone: UpdatePhone
    ): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("api/v1/user/updateProfile/{id}")
    fun updateUPI(
        @Header("Authorization") accessToken: String,
        @Path("id") userId: String,
        @Body userUPI: UpdateUPI
    ): Call<Void>
}
