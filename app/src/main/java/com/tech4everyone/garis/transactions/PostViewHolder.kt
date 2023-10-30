package com.tech4everyone.garis.transactions

import android.icu.text.NumberFormat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tech4everyone.garis.R
import java.util.Locale


class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val postTitle: TextView = itemView.findViewById(R.id.title)
    private val postNumber: TextView = itemView.findViewById(R.id.number)
//    private val delete: Button = itemView.findViewById(R.id.btn_delete)


    fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
        postTitle.text = post.title

        fun rupiah(number: Int): String{
            val localeID =  Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            return numberFormat.format(number).toString()
        }

        postNumber.text = rupiah(post.number!!)

//        delete.setOnClickListener(starClickListener)
    }
//
//    fun setLikedState(liked: Boolean) {
//        if (liked) {
//            star.setImageResource(R.drawable.ic_toggle_star_24)
//        } else {
//            star.setImageResource(R.drawable.ic_toggle_star_outline_24)
//        }
//    }
}
