package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.remote.model.LoginRequest
import com.devpro.sound.data.remote.model.LoginResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource (
    private val firebaseAuth: FirebaseAuth
){
    suspend fun login(request: LoginRequest): LoginResponse{
        return try{
            val result = firebaseAuth
                .signInWithEmailAndPassword(
                    request.email,
                    request.password
                ).await()
            LoginResponse(
                userId = result.user?.uid,
                email = result.user?.email,
                isSuccess = true
            )
        }catch (exception: Exception) {
            LoginResponse(
                userId = null,
                email = null,
                isSuccess = false,
                message = exception.message
            )
        }
    }
}