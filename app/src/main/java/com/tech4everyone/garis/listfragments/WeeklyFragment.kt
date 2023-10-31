package com.tech4everyone.garis.listfragments

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class WeeklyFragment : PostListFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getQuery(databaseReference: DatabaseReference): Query {
        // My top posts by number of stars
        val myUserId = uid

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeZone = TimeZone.getTimeZone("Asia/Jakarta")

        val startOfWeek = Calendar.getInstance(timeZone).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }.time

        val endOfWeek = Calendar.getInstance(timeZone).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }.time

        val startOfWeekAsString = dateFormat.format(startOfWeek)
        val endOfWeekAsString = dateFormat.format(endOfWeek)

        return databaseReference.child("user-posts").child(myUserId)
            .orderByChild("date").startAt(startOfWeekAsString).endAt(endOfWeekAsString)

    }
}
