package com.example.ichiba.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AuthTokenDao {
    @Query("SELECT * FROM auth_token LIMIT 1")
    suspend fun getAuthToken(): AuthToken?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthToken(authToken: AuthToken)

    @Delete
    suspend fun deleteAuthToken(authToken: AuthToken)

    @Query("SELECT userId FROM auth_token LIMIT 1")
    suspend fun getUserId(): Int?

    @Update
    suspend fun updateAuthToken(authToken: AuthToken)
}