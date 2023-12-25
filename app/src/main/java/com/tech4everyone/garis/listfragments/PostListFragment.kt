package com.tech4everyone.garis.listfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.tech4everyone.garis.DetailTransactionFragment
import com.tech4everyone.garis.R
import com.tech4everyone.garis.databinding.FragmentAllPostsAltBinding
import com.tech4everyone.garis.databinding.FragmentAllPostsBinding
import com.tech4everyone.garis.rupiah
import com.tech4everyone.garis.transactions.Post
import com.tech4everyone.garis.transactions.PostViewHolder


abstract class PostListFragment : Fragment() {
    private var _binding: FragmentAllPostsBinding? = null
    private val binding get() = _binding!!

    private val listTransactions: MutableList<Post?> = ArrayList()
    private var totalNominal: Int = 0
    // [START define_database_reference]
    private lateinit var database: DatabaseReference
    // [END define_database_reference]

    private lateinit var recycler: RecyclerView
    private lateinit var manager: LinearLayoutManager
    private var adapter: FirebaseRecyclerAdapter<Post, PostViewHolder>? = null

    val uid: String
        get() = Firebase.auth.currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        // [START create_database_reference]
        database = Firebase.database.reference
        // [END create_database_reference]

        recycler = binding.root.findViewById(R.id.messagesList)
        recycler.setHasFixedSize(true)

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set up Layout Manager, reverse layout
        manager = LinearLayoutManager(activity)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        recycler.layoutManager = manager

        // Set up FirebaseRecyclerAdapter with the Query
        val postsQuery = getQuery(database)

        val options = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(postsQuery, Post::class.java)
            .build()

        listeningTopBar()

        adapter = object : FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PostViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)
                return PostViewHolder(inflater.inflate(R.layout.item_transaction, viewGroup, false))
            }

            override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int, model: Post) {
                val postRef = getRef(position)

                // Set click listener for the whole post view
                val postKey = postRef.key
                viewHolder.itemView.setOnClickListener {
                    // Launch PostDetailFragment
                    val navController = requireActivity().findNavController(R.id.nav_host_fragment)
                    val args = bundleOf(DetailTransactionFragment.EXTRA_POST_KEY to postKey)
                    navController.navigate(R.id.action_MainFragment_to_PostDetailFragment, args)
                }

                viewHolder.bindToPost(model) {
                    // Need to write to both places the post is stored
                    val globalPostRef = database.child("posts").child(postRef.key!!)
                    val userPostRef = database.child("user-posts").child(model.uid!!).child(postRef.key!!)

                    // Run two transactions
//                    onStarClicked(globalPostRef)
//                    onStarClicked(userPostRef)
                }
            }
        }
        recycler.adapter = adapter
    }

    private fun listeningTopBar() {
        binding.jumlahTransaksi.text = "0"
        binding.nominalTotal.text = "0"
        listTransactions.clear()
        totalNominal = 0

        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                binding.topBarMain.visibility = View.VISIBLE
                binding.progressBarTop.visibility = View.GONE
                for (ds in dataSnapshot.children) {
                    val data = ds.getValue<Post>()
                    data?.let { x ->
                        listTransactions.add(x)
                    }
                }

                // setup topbar
                if (listTransactions.size > 0) {
                    listTransactions.forEachIndexed { index, post ->
                        totalNominal += post?.number!!
                    }
                }

                binding.jumlahTransaksi.text = "${listTransactions.size} transaksi"
                binding.nominalTotal.text = rupiah(totalNominal)

                Log.d(listTransactions.size.toString(), TAG)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                Toast.makeText(
                    context,
                    "Failed to load post.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        getQuery(database).addListenerForSingleValueEvent(eventListener)
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onResume() {
        super.onResume()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    abstract fun getQuery(databaseReference: DatabaseReference): Query

    companion object {
        private const val TAG = "PostListFragment"
    }
}
