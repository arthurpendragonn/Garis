package com.tech4everyone.garis.transactions

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.util.MurmurHash3
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.tech4everyone.garis.listfragments.PostListFragment
import com.tech4everyone.garis.rupiah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val listTransactions: MutableList<Post> = ArrayList()
    fun fetchData(postQuery: Query): MutableList<Post> {
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {
                    val data = ds.getValue<Post>()
                    data?.let { x ->
                        listTransactions.add(x)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Getting Data Firebase", "loadPost:onCancelled", databaseError.toException())

            }
        }

        postQuery.addListenerForSingleValueEvent(eventListener)

        return listTransactions
    }
}