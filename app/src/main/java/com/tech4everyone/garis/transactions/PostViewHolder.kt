package com.tech4everyone.garis.transactions

import android.icu.text.NumberFormat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tech4everyone.garis.R
import com.tech4everyone.garis.rupiah
import java.util.Locale


class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val postTitle: TextView = itemView.findViewById(R.id.title)
    private val postNumber: TextView = itemView.findViewById(R.id.number)
    private val postDate: TextView = itemView.findViewById(R.id.dateItem)


    fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
        postTitle.text = post.title
        postDate.text = post.date

        postNumber.text = rupiah(post.number!!)
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
