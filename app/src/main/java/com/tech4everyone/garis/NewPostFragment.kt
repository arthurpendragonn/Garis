package com.tech4everyone.garis

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.navigation.fragment.findNavController
import androidx.transition.Visibility
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.Firebase
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tech4everyone.garis.databinding.FragmentNewPostBinding
import com.tech4everyone.garis.transactions.Post
import com.tech4everyone.garis.transactions.User
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Date

class NewPostFragment : BaseFragment() {
    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    private var getFile: Uri? = null
    private var getUrlFile: String? = null

    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        activity?.actionBar?.title = "Unggah Transaksi"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Firebase.database.reference
        storage = Firebase.storage
        storageRef = storage.reference

        binding.fabSubmitPost.setOnClickListener { submitPost() }
        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_NewPostFragment_to_MainFragment)

        }

        binding.photo.setOnClickListener { startGallery() }

    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            //mlkit

            val selectedImg: Uri = result.data?.data as Uri

            val myFile = uriToFile(selectedImg, requireContext())

            getFile = selectedImg
            binding.photo.setImageURI(selectedImg)
            textRecognition(selectedImg)


        }
    }

    private fun textRecognition(img: Uri) {
        onLoading()

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val image: InputImage
        try {
            image = InputImage.fromFilePath(requireContext(), img)

            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Task completed successfully
                    // ...
                    onFailed()
                    binding.fieldBody.text.clear()

                    if (visionText != null) {
                        val resultText = visionText.text
                        val multipleNominal: MutableList<Int> = ArrayList()

                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val lineText = line.text

                                val removedDots = lineText.replace(".", "")
                                val removedColumn = removedDots.replace(",", "")
                                val removedSpace = removedColumn.replace(" ", "")
                                val charArr = removedSpace.toCharArray()


                                if (charArr[0] == 'R') {
                                    if (charArr[1] == 'p') {
                                        val nominal = removedSpace.filter { it -> it.isDigit() }

                                        if (nominal != "")  {
                                            multipleNominal.add(nominal.toInt())
                                            binding.fieldBody.setText(nominal)
                                            onSuccess()
                                        }
                                    }
                                }
                            }
                        }

                        if (multipleNominal.size > 1) {
                            val max: Int = multipleNominal.maxOrNull() ?: 0
                            binding.fieldBody.setText(max.toString())
                            onSuccess()
                        }
                    }

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                    Toast.makeText(requireContext(), "$e", Toast.LENGTH_SHORT).show()

                }

        } catch (e: IOException) {

            e.printStackTrace()
        }


    }

    private fun submitPost() {
        val title = binding.fieldTitle.text.toString()
        val body = binding.fieldBody.text.toString()
        val provider = binding.provider.text.toString()

        // Title is required
        if (TextUtils.isEmpty(title)) {
            binding.fieldTitle.error = REQUIRED
            return
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            binding.fieldBody.error = REQUIRED
            return
        }

        if (body.toLong() > 2147483647) {
            binding.fieldBody.error = "Nominal Kebesaran"
            return
        }

        if (TextUtils.isEmpty(provider)) {
            binding.provider.error = REQUIRED
            return
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false)
        Toast.makeText(context, "Posting...", Toast.LENGTH_SHORT).show()

        val userId = uid
        database.child("users").child(userId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get user value
                    val user = dataSnapshot.getValue<User>()

                    if (user == null) {
                        // User is null, error out
                        Log.e(TAG, "User $userId is unexpectedly null")
                        Toast.makeText(
                            context,
                            "Error: could not fetch user.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        // Write new post
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val current = LocalDateTime.now().format(formatter)

                        //upload to firebase storage
                        if (getFile != null) {
                            val transactionsRef = storageRef.child("images/${getFile?.lastPathSegment}")

                            val uploadTask = getFile?.let { transactionsRef.putFile(it) }
                            uploadTask?.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                transactionsRef.downloadUrl
                            }?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    writeNewPost(userId, title, body.toInt(), provider, current, task.result.toString())
                                    Log.d(TAG, task.result.toString())

                                } else {
                                    // Handle failures
                                    Log.e(TAG, "error")
                                }
                            }
                        } else {
                            writeNewPost(userId, title, body.toInt(), provider, current, "")
                        }
                    }

                    setEditingEnabled(true)
                    findNavController().navigate(R.id.action_NewPostFragment_to_MainFragment)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "getUser:onCancelled", databaseError.toException())
                    setEditingEnabled(true)
                }
            },
        )
    }

    private fun setEditingEnabled(enabled: Boolean) {
        with(binding) {
            fieldTitle.isEnabled = enabled
            fieldBody.isEnabled = enabled
            if (enabled) {
                fabSubmitPost.show()
            } else {
                fabSubmitPost.hide()
            }
        }
    }

    private fun writeNewPost(userId: String, title: String, number: Int, provider: String, date: String, fileUrl: String?) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        val key = database.child("posts").push().key
        if (key == null) {
            Log.w(TAG, "Couldn't get push key for posts")
            return
        }

        val post = Post(userId, title, number, provider, date, fileUrl)
        val postValues = post.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/posts/$key" to postValues,
            "/user-posts/$userId/$key" to postValues,
        )

        database.updateChildren(childUpdates)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onLoading() {
        binding.tv1.visibility = View.GONE
        binding.tv2.visibility = View.GONE
        binding.prograss.visibility = View.VISIBLE
        binding.succes.visibility = View.GONE
        binding.fail.visibility = View.GONE

    }

    private fun onSuccess() {
        binding.tv1.visibility = View.GONE
        binding.tv2.visibility = View.GONE
        binding.prograss.visibility = View.GONE
        binding.succes.visibility = View.VISIBLE
        binding.fail.visibility = View.GONE
    }

    private fun onFailed() {
        binding.tv1.visibility = View.GONE
        binding.tv2.visibility = View.GONE
        binding.prograss.visibility = View.GONE
        binding.succes.visibility = View.GONE
        binding.fail.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "NewPostFragment"
        private const val REQUIRED = "Required"
    }
}
