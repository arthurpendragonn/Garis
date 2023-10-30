package com.tech4everyone.garis.transactions

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.Date
import java.util.HashMap

@IgnoreExtraProperties
data class Post(
    var uid: String? = "",
    var title: String? = "",
    var number: Int? = 0
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "title" to title,
            "number" to number
        )
    }
}
