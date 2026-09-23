package com.devpro.sound.data.remote.datasource

import com.devpro.sound.data.remote.model.LoginRequest
import com.devpro.sound.data.remote.model.LoginResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource (
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
                message = userMessage(exception)
            )
        }
    }

    suspend fun register(request: LoginRequest): LoginResponse {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(request.email, request.password)
                .await()
            val user = result.user
                ?: throw IllegalStateException("Không tạo được tài khoản")
            val displayName = request.name.trim()

            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            ).await()

            firestore
                .collection(USERS_COLLECTION)
                .document(user.uid)
                .set(
                    mapOf(
                        "name" to displayName,
                        "accountSubtitle" to "@${request.email.substringBefore("@")}"
                    ),
                    SetOptions.merge()
                )
                .await()

            LoginResponse(
                userId = user.uid,
                email = user.email,
                isSuccess = true,
                message = "Đăng ký thành công"
            )
        } catch (exception: Exception) {
            LoginResponse(
                userId = null,
                email = null,
                isSuccess = false,
                message = userMessage(exception)
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
                message = userMessage(exception)
            )
        }
    }

    private fun userMessage(exception: Exception): String {
        return when ((exception as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Email không hợp lệ"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email này đã được đăng ký"
            "ERROR_WEAK_PASSWORD" -> "Mật khẩu quá yếu, hãy dùng ít nhất 6 ký tự"
            "ERROR_USER_NOT_FOUND",
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL" -> "Email hoặc mật khẩu không chính xác"
            "ERROR_TOO_MANY_REQUESTS" -> "Có quá nhiều yêu cầu, hãy thử lại sau"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Không có kết nối mạng"
            else -> exception.message ?: "Đã xảy ra lỗi xác thực"
        }
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
