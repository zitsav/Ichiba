package com.example.ichiba.interfaces

import com.example.ichiba.dataclass.Product
import com.example.ichiba.dataclass.ProductRequest
import com.example.ichiba.dataclass.ProductResponse
import com.example.ichiba.dataclass.SearchRequest
import com.example.ichiba.dataclass.UpdateProductRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductApi {
    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @GET("api/v1/products")
    fun getAllProducts(@Header("Authorization") accessToken: String): Call<ProductResponse>


    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @GET("api/v1/products/user")
    fun getAllProductsOfUser(@Header("Authorization") accessToken: String): Call<ProductResponse>


    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("api/v1/products/search")
    fun searchProducts(
        @Header("Authorization") accessToken: String,
        @Body request: SearchRequest
    ): Call<ProductResponse>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @GET("api/v1/products/{id}")
    fun getProductById(
        @Header("Authorization") accessToken: String,
        @Path("id") productId: String
    ): Call<Product>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @POST("api/v1/products")
    fun createProduct(
        @Header("Authorization") accessToken: String,
        @Body request: ProductRequest
    ): Call<Product>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @PUT("api/v1/products/{id}")
    fun updateProduct(
        @Header("Authorization") accessToken: String,
        @Path("id") id: String,
        @Body request: UpdateProductRequest
    ): Call<Void>

    @Headers(
        "accept: */*",
        "accept-encoding: gzip, deflate, br",
        "content-type: application/json",
    )
    @DELETE("api/v1/products/{id}")
    fun deleteProduct(
        @Header("Authorization") accessToken: String,
        @Path("id") id: String
    ): Call<Void>
}