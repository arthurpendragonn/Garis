package com.tech4everyone.garis.listfragments

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class YearlyFragment : PostListFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getQuery(databaseReference: DatabaseReference): Query {
        val myUserId = uid

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val month = LocalDate.now().month
        val year = LocalDate.now().year
        val startYear = LocalDate.of(year, month, 1).format(formatter)
        val endYear = LocalDate.of(year+1, month, 1).format(formatter)


        return databaseReference.child("user-posts").child(myUserId)
            .orderByChild("date").startAt(startYear).endAt(endYear)
    }
}
