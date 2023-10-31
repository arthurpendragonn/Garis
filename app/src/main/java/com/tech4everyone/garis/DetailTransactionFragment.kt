package com.tech4everyone.garis

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.tech4everyone.garis.databinding.FragmentDetailTransactionBinding
import com.tech4everyone.garis.transactions.Post
import java.lang.IllegalArgumentException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailTransactionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailTransactionFragment : BaseFragment() {
    private lateinit var postKey: String
    private lateinit var postReference: DatabaseReference
    private lateinit var userPostReference: DatabaseReference

    private var postListener: ValueEventListener? = null

    private var _binding: FragmentDetailTransactionBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get post key from arguments
        postKey = requireArguments().getString(EXTRA_POST_KEY)
            ?: throw IllegalArgumentException("Must pass EXTRA_POST_KEY")

        // Initialize Database
        postReference = Firebase.database.reference
            .child("posts").child(postKey)
        userPostReference = Firebase.database.reference
            .child("user-posts").child(uid).child(postKey)

        binding.delete.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure you want to Delete?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // Delete selected data
                    postReference.removeValue().addOnCompleteListener {
                        if (it.isSuccessful) {
                            userPostReference.removeValue().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(requireContext(), "Data Terhapus", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_DetailTransactionFragment_to_MainFragment)
                                } else {
                                    Toast.makeText(requireContext(), "Terjadi error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Terjadi error", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                .setNegativeButton("No") { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_DetailTransactionFragment_to_MainFragment)
        }
    }

    override fun onStart() {
        super.onStart()

        // Add value event listener to the post
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue<Post>()
                post?.let {
                    with(binding) {
                        title.text = it.title
                        number.text = rupiah(it.number!!)
                        provider.text = it.provider
                        date.text = it.date
                    }

                    if (it.fileUrl != "") {
                        Glide.with(requireContext())
                            .load(it.fileUrl).dontTransform()
                            .into(binding.photo)

                        binding.attach.text = "Terdapat file terkait"
                    } else
                        binding.attach.text= "Tidak terdapat file terkait"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                Toast.makeText(
                    context,
                    "Failed to load post.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
        postReference.addValueEventListener(postListener)

        // Keep copy of post listener so we can remove it when app stops
        this.postListener = postListener

    }

    override fun onStop() {
        super.onStop()

        // Remove post value event listener
        postListener?.let {
            postReference.removeEventListener(it)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    companion object {
        private const val TAG = "PostDetailFragment"
        const val EXTRA_POST_KEY = "post_key"
    }
}