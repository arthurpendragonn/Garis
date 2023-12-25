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
        val dec = LocalDate.of(year, 12, 1)
        val startMonth = LocalDate.of(year, month, 1).format(formatter)
        val endMonth = if (month == dec.month) {
            LocalDate.of(year, month, 31).format(formatter)
        } else {
            LocalDate.of(year, month + 1, 1).format(formatter)
        }

//        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
//        val timeZone = TimeZone.getTimeZone("Asia/Jakarta")
//
//        val startMonth = Calendar.getInstance(timeZone).apply {
//            set(Calendar.YEAR, Calendar.MONTH, 1)
//        }.time
//
//        val endOfMonth = Calendar.getInstance(timeZone).apply {
//            set(Calendar.YEAR, Calendar.MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
//        }.time
//
//        val startMonthAsString = dateFormat.format(startMonth)
//        val endMonthAsString = dateFormat.format(endOfMonth)

        return databaseReference.child("user-posts").child(myUserId)
            .orderByChild("date").startAt(startMonth).endAt(endMonth)
    }
}
