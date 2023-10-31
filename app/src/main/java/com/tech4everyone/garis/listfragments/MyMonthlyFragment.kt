package com.tech4everyone.garis.listfragments

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

class MyMonthlyFragment : PostListFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getQuery(databaseReference: DatabaseReference): Query {
        // My top posts by number of stars
        val myUserId = uid

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val month = LocalDate.now().month
        val year = LocalDate.now().year
        val startMonth = LocalDate.of(year, month, 1).format(formatter)
        val endMonth = LocalDate.of(year, month + 1, 1).format(formatter)


        return databaseReference.child("user-posts").child(myUserId)
            .orderByChild("date").startAt(startMonth).endAt(endMonth)
    }
}
