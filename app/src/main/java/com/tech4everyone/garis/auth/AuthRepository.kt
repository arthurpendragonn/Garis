package com.tech4everyone.garis.auth

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.tech4everyone.garis.transactions.User

class AuthRepository {

    private val _authState by lazy { MutableLiveData<AuthState>(AuthState.Idle) }
    val authState: LiveData<AuthState> = _authState

    private var _loginState = MutableLiveData(false)
    val loginState = _loginState

    private val auth = FirebaseAuth.getInstance()

    private val database = Firebase.database.reference

    fun handleSignUp(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading

        val profileUpdates = userProfileChangeRequest {
            displayName = name
            photoUri = Uri.parse("https://upload.wikimedia.org/wikipedia/commons/6/68/Joe_Biden_presidential_portrait.jpg")
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG,"Email signup is successful")

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i(TAG,"Email Signing is successful")

                                auth.currentUser?.updateProfile(profileUpdates)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.i(TAG,"Profile signup is successful")
                                            _authState.value = AuthState.Success
                                            auth.signOut()
                                        } else {
                                            task.exception?.let {
                                                Log.i(TAG,"Profile signup failed with error ${it.localizedMessage}")
                                                _authState.value =
                                                    AuthState.Error(it.localizedMessage)
                                                auth.signOut()
                                            }
                                        }
                                    }
                            } else {
                                task.exception?.let {
                                    Log.i(TAG,"Email Signing failed with error ${it.localizedMessage}")
                                    _authState.value = AuthState.Error(it.localizedMessage)
                                }
                            }
                        }
                } else {
                    task.exception?.let {
                        Log.i(TAG,"Email signup failed with error ${it.localizedMessage}")
                        _authState.value = AuthState.Error(it.localizedMessage)
                    }
                }
            }
    }

    fun handSignIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG,"Email Signing is successful")
                    _authState.value = AuthState.Success

                    onAuthSuccess(task.result?.user!!)
                } else {
                    task.exception?.let {
                        Log.i(TAG,"Email Signing failed with error ${it.localizedMessage}")
                        _authState.value = AuthState.Error(it.localizedMessage)
                    }
                }
            }
    }

    fun getLoginState() {
        _loginState.value = auth.currentUser != null
    }

    fun getUser() = auth.currentUser

    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG,"Message successfully sent")
            } else {
                it.exception?.let {
                    Log.i(TAG, "Reset Password Error ${it.localizedMessage}")
                    _authState.value = AuthState.Error(it.localizedMessage)
                }
            }
        }
    }

    fun updateProfile(name: String) {

        val profileUpdates = userProfileChangeRequest {
            displayName = name
            photoUri = Uri.parse("https://upload.wikimedia.org/wikipedia/commons/6/68/Joe_Biden_presidential_portrait.jpg")
        }

        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG,"Profile update is successful")
                    _authState.value = AuthState.Success
                } else {
                    task.exception?.let {
                        Log.i(TAG,"Profile update failed with error ${it.localizedMessage}")
                        _authState.value = AuthState.Error(it.localizedMessage)
                    }
                }
            }
    }

    private fun onAuthSuccess(user: FirebaseUser) {
        val username = usernameFromEmail(user.email!!)

        // Write new user
        writeNewUser(user.uid, username, user.email)
    }

    private fun usernameFromEmail(email: String): String {
        return if (email.contains("@")) {
            email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            email
        }
    }

    private fun writeNewUser(userId: String, name: String, email: String?) {
        val user = User(name, email)
        database.child("users").child(userId).setValue(user)
    }

    fun signOut() = auth.signOut()

    companion object{
        const val TAG = "Auth Repository"
    }
}