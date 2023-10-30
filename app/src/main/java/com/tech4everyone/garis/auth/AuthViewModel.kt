package com.tech4everyone.garis.auth

import androidx.lifecycle.ViewModel

class AuthViewModel: ViewModel() {

    private val repository = AuthRepository()

    val authState = repository.authState

    val loginState = repository.loginState

    fun handleSignIn(email: String, password: String) {
        repository.handSignIn(email, password)
    }

    fun handleSignUp(email: String, password: String, name: String) {
        repository.handleSignUp(email, password, name)
    }

    fun getLoginState() = repository.getLoginState()

    fun getUser() = repository.getUser()

    fun resetPassword(email: String) = repository.resetPassword(email)
}