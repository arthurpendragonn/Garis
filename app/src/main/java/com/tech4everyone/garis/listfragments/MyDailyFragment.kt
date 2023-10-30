package com.tech4everyone.garis.listfragments

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyDailyFragment : PostListFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getQuery(databaseReference: DatabaseReference): Query {
        // My top posts by number of stars
        val myUserId = uid

        val dayFormat = DateTimeFormatter.ofPattern("dd")
        val day = LocalDateTime.now().format(dayFormat)

        return databaseReference.child("user-posts").child(myUserId)

    }
}
