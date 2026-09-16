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

    suspend fun register(request: LoginRequest): LoginResponse {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(request.email, request.password)
                .await()
            LoginResponse(
                userId = result.user?.uid,
                email = result.user?.email,
                isSuccess = true,
                message = "Đăng ký thành công"
            )
        } catch (exception: Exception) {
            LoginResponse(
                userId = null,
                email = null,
                isSuccess = false,
                message = exception.message
            )
        }
    }

    suspend fun sendPasswordResetEmail(email: String): LoginResponse {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            LoginResponse(
                userId = null,
                email = email,
                isSuccess = true,
                message = "Đã gửi email đặt lại mật khẩu"
            )
        } catch (exception: Exception) {
            LoginResponse(
                userId = null,
                email = email,
                isSuccess = false,
                message = exception.message
            )
        }
    }
}
