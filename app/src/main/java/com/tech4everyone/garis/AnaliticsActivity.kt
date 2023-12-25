package com.tech4everyone.garis

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.tech4everyone.garis.databinding.ActivityAnaliticsBinding
import com.tech4everyone.garis.databinding.ActivityMainBinding
import com.tech4everyone.garis.listfragments.PostListFragment
import com.tech4everyone.garis.transactions.Post

class AnaliticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnaliticsBinding

    val uid: String
        get() = Firebase.auth.currentUser!!.uid

    // on below line we are creating
    // variables for our bar chart
    lateinit var barChart: BarChart

    // on below line we are creating
    // a variable for bar data
    lateinit var barData: BarData

    // on below line we are creating a
    // variable for bar data set
    lateinit var barDataSet: BarDataSet

    // on below line we are creating array list for bar data
    lateinit var barEntriesList: ArrayList<BarEntry>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnaliticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        barChart = binding.barChart
        // on below line we are calling get bar
        // chart data to add data to our array list
        getBarChartData()

        // on below line we are initializing our bar data set
        barDataSet = BarDataSet(barEntriesList, "Bar Chart Data")

        // on below line we are initializing our bar data
        barData = BarData(barDataSet)

        // on below line we are setting data to our bar chart
        barChart.data = barData

        // on below line we are setting colors for our bar chart text
        barDataSet.valueTextColor = Color.BLACK

        // on below line we are setting color for our bar data set
        barDataSet.setColor(resources.getColor(R.color.colorPrimary))

        // on below line we are setting text size
        barDataSet.valueTextSize = 16f

        // on below line we are enabling description as false
        barChart.description.isEnabled = false

    }

    private fun getBarChartData() {
        barEntriesList = ArrayList()

        val userPostReference = Firebase.database.reference
            .child("user-posts").child(uid)

        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val data = ds.getValue<Post>()
                    data?.let { x ->
//                        barEntriesList.add(BarEntry(11f, x.number?.toFloat()!!))
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                Toast.makeText(
                    this@AnaliticsActivity,
                    "Failed to load post.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        userPostReference.addListenerForSingleValueEvent(eventListener)

        // on below line we are adding data
        // to our bar entries list
        barEntriesList.add(BarEntry(1f, 1f))
        barEntriesList.add(BarEntry(2f, 2f))
        barEntriesList.add(BarEntry(3f, 3f))
        barEntriesList.add(BarEntry(4f, 4f))
        barEntriesList.add(BarEntry(5f, 5f))
        // on below line we are adding data
        // to our bar entries list

    }

    companion object {
        private const val TAG = "AnalisticActivity"
    }
}
