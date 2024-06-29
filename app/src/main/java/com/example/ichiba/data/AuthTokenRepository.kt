package com.example.ichiba.data

import com.example.ichiba.dataclass.User
import java.sql.Time

class AuthTokenRepository(private val authTokenDao: AuthTokenDao) {
    suspend fun saveAuthToken(
        token: String,
        expirationTime: Time,
        user: User
    ) {
        val authToken = AuthToken(
            token = token,
            expirationTime = expirationTime,
            userId = user.userId,
            name = user.name,
            enrollmentNumber = user.enrollmentNumber,
            program = user.program,
            batchYear = user.batchYear,
            batch = user.batch,
            upiId = user.upiId,
            phoneNumber = user.phoneNumber,
            profilePicture = user.profilePicture
        )
        authTokenDao.insertAuthToken(authToken)
    }

    suspend fun getAuthToken(): AuthToken? {
        return authTokenDao.getAuthToken()
    }

    suspend fun getUserId(): Int? {
        return authTokenDao.getUserId()
    }

    suspend fun deleteAuthToken(authToken: AuthToken) {
        authTokenDao.deleteAuthToken(authToken)
    }

    suspend fun updateAuthToken(authToken: AuthToken) {
        authTokenDao.updateAuthToken(authToken)
    }
}