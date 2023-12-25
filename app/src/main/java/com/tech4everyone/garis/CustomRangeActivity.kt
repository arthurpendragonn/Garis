package com.tech4everyone.garis

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.tech4everyone.garis.databinding.ActivityCustomRangeBinding
import com.tech4everyone.garis.transactions.MainViewModel
import com.tech4everyone.garis.transactions.Post
import com.tech4everyone.garis.transactions.PostAdapter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CustomRangeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomRangeBinding
    private val listTransactions: MutableList<Post> = ArrayList()
    private var totalNominal: Int = 0
    private val viewModel: MainViewModel by viewModels()

    // [START define_database_reference]
    private lateinit var database: DatabaseReference
    // [END define_database_reference]
    private val adapter = PostAdapter()

    val uid: String
        get() = Firebase.auth.currentUser!!.uid

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomRangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference

//        showRecyclerView()
        // Write new post
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

        binding.tvStartDatePicker.text = startMonth
        binding.tvEndDatePicker.text = endMonth

        binding.tvStartDatePicker.setOnClickListener {
            showDatePicker(true)
        }
        binding.tvEndDatePicker.setOnClickListener {
            showDatePicker(false)
        }

        showRecyclerView()

        val postQuery = database.child("user-posts").child(uid)
            .orderByChild("date").startAt(startMonth).endAt(endMonth)
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                listTransactions.clear()
                for (ds in dataSnapshot.children) {
                    val data = ds.getValue<Post>()
                    data?.let { x ->
                        listTransactions.add(x)
                    }
                }

                adapter.setData(listTransactions);
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Getting Data Firebase", "loadPost:onCancelled", databaseError.toException())

            }
        }

        postQuery.addListenerForSingleValueEvent(eventListener)


        //adapter.setData(viewModel.fetchData(postQuery))



        binding.tvSampai.setOnClickListener {
            val startDate = binding.tvStartDatePicker.text.toString()
            val endDate = binding.tvEndDatePicker.text.toString()

            val postQuery = database.child("user-posts").child(uid)
                .orderByChild("date").startAt(startDate).endAt(endDate)

            //adapter.setData(viewModel.fetchData(postQuery))

            val eventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    listTransactions.clear()
                    for (ds in dataSnapshot.children) {
                        val data = ds.getValue<Post>()
                        data?.let { x ->
                            listTransactions.add(x)
                        }
                    }

                    adapter.setData(listTransactions);
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Getting Data Firebase", "loadPost:onCancelled", databaseError.toException())

                }
            }

            postQuery.addListenerForSingleValueEvent(eventListener)
        }
    }

    private fun showRecyclerView() {
        binding.apply {
            messagesList.layoutManager =
                if (applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    GridLayoutManager(this@CustomRangeActivity, 2)
                } else {
                    LinearLayoutManager(this@CustomRangeActivity)
                }

            messagesList.setHasFixedSize(true)
            messagesList.adapter = adapter
        }
    }



    private fun showDatePicker(start: Boolean) {
        val calendar = Calendar.getInstance()
        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix

                if (start) binding.tvStartDatePicker.text = formattedDate
                else binding.tvEndDatePicker.text = formattedDate

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}