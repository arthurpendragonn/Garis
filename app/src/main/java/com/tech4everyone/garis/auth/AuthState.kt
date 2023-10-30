package com.tech4everyone.garis.auth

sealed class AuthState(val message: String? = null) {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    class Error(message: String?) : AuthState(message)
}