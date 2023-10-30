package com.tech4everyone.garis.transactions

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var username: String? = "",
    var email: String? = "",
)
