package com.tech4everyone.garis

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

open class BaseFragment : Fragment() {
    private var progressBar: ProgressBar? = null

    val uid: String
        get() = Firebase.auth.currentUser!!.uid

    fun setProgressBar(resId: Int) {
        progressBar = view?.findViewById(resId)
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }
}
