package com.tech4everyone.garis

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
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
import java.io.IOException
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.log

class NewPostFragment : BaseFragment() {
    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    private var getFile: Uri? = null
    private var getUrlFile: String? = null
    private var userSelectedDate: String = ""
    private val multipleNominal: MutableList<Int> = ArrayList()


    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Tambah Transaksi"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Firebase.database.reference
        storage = Firebase.storage
        storageRef = storage.reference

        // Write new post
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val current = LocalDateTime.now().format(formatter)
        binding.tvDatePicker.text = current

        binding.fabSubmitPost.setOnClickListener { submitPost() }
        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_NewPostFragment_to_MainFragment)
        }

        binding.photo.setOnClickListener { startGallery() }
        binding.addImageLayout.setOnClickListener { startGallery() }

        binding.tvDatePicker.setOnClickListener {
            showDatePicker()
        }

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
            val selectedImg: Uri = result.data?.data as Uri

            val myFile = uriToFile(selectedImg, requireContext())

            getFile = selectedImg
            binding.photo.setImageURI(selectedImg)
            textRecognition(selectedImg)

        }
    }

    private fun textRecognition(img: Uri) {

        //provider
        binding.provider.text.clear()
        binding.succesProvider.visibility = View.GONE
        binding.failProvider.visibility = View.VISIBLE

        //nominal
        binding.fieldBody.text.clear()
        multipleNominal.clear()
        onLoading()
        onFailed()

        //date
        binding.successDate.visibility = View.GONE
        binding.failDate.visibility = View.VISIBLE

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image: InputImage

        var textLine = ""

        try {
            image = InputImage.fromFilePath(requireContext(), img)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (visionText != null){
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                textLine += "${line.text}\n"

                                nominalRecognition(line.text)
                                providerRecognition(line.text)
                                dateRecognition(line.text)

                            }
                        }

                        if (multipleNominal.size > 1) {
                            val max: Int = multipleNominal.maxOrNull() ?: 0
                            binding.fieldBody.setText(max.toString())
                            onSuccess()
                        }
                    }
                    Log.d("Text Recognition", textLine)
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "$e", Toast.LENGTH_SHORT).show()
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
    private fun providerRecognition(lineText: String) {

        when (lineText) {

            //E-Wallet
            //OVO
            "OVO", "OVO Cash", "Masuk ke OVO Cash", "OVO Cash Terpakai" -> {
                binding.provider.setText("OVO")
                binding.succesProvider.visibility = View.VISIBLE
                binding.failProvider.visibility = View.GONE

            }

            //Gopay
            "GoPay Saldo", "GoPay Coins", "GoPay Pinjam" -> {
                binding.provider.setText("Gopay")
                binding.succesProvider.visibility = View.VISIBLE
                binding.failProvider.visibility = View.GONE

            }

            //Dana
            "ODANA", "Saldo DANA", "DANA Balance", "Powered by DAnA" -> {
                binding.provider.setText("DANA")
                binding.succesProvider.visibility = View.VISIBLE
                binding.failProvider.visibility = View.GONE

            }

            //ShopeePay
            "ShopeePay", "Wallet Balance", "Promo ShopeePay" -> {
                binding.provider.setText("ShopeePay")
                binding.succesProvider.visibility = View.VISIBLE
                binding.failProvider.visibility = View.GONE

            }
        }
    }

    private fun nominalRecognition(lineText: String) {
        var processedText = lineText

        val removeTwoZero = lineText.toCharArray()
        val sizeChar = removeTwoZero.size
        if (sizeChar > 2) {
            if (removeTwoZero[sizeChar - 1] == '0'
                && removeTwoZero[sizeChar - 2] == '0') {
                if (removeTwoZero[sizeChar - 3] == '.' ||
                    removeTwoZero[sizeChar - 3] == ',') {

                    processedText = processedText.dropLast(2)
                }
            }
        }

        processedText = processedText
            .replace("TOTAL PEMBAYARAN:", "")
            .replace(".", "")
            .replace(",", "")
            .replace("+", "")
            .replace("-", "")
            .replace(" ", "")

        val charArr = processedText.toCharArray()

        if (charArr.size > 2) {
            if (charArr[0] == 'R') {
                if (charArr[1] == 'p' || charArr[1] == 'P') {
                    val nominal = processedText.filter { it -> it.isDigit() }

                    if (nominal != "")  {
                        multipleNominal.add(nominal.toInt())
                        binding.fieldBody.setText(nominal)
                        onSuccess()
                    }
                }
            } else if (charArr[0] == 'I'
                && charArr[1] == 'D'
                && charArr[2] == 'R') {
                val nominal = processedText.filter { it -> it.isDigit() }

                if (nominal != "")  {
                    multipleNominal.add(nominal.toInt())
                    binding.fieldBody.setText(nominal)
                    onSuccess()
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateRecognition(lineText: String) {

        val submitFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cleanedString = lineText.replace("·", " ")
            .replace("-", " ")
            .replace(",", " ")
            .replace(".", " ")

        fun parseDateEn(dateString: String): Date? {

            val dateFormatEn = SimpleDateFormat("dd MMM yyyy", Locale("en", "EN"))

            return try {
                dateFormatEn.parse(dateString)
            } catch (e: Exception) {
                // Handle parsing exceptions if necessary
                Log.d("DATE RECOGNITION","Error parsing date for input: $dateString")
                null
            }
        }

        fun parseDateId(dateString: String): Date? {

            val dateFormatId = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

            return try {
                dateFormatId.parse(dateString)
            } catch (e: Exception) {
                // Handle parsing exceptions if necessary
                Log.d("DATE RECOGNITION","Error parsing date for input: $dateString")
                null
            }
        }

        fun parseDateShopeePayPattern(dateString: String): Date? {

            val dateFormatId = SimpleDateFormat("dd MM yyyy")

            return try {
                val dateTimeSubstring = dateString.substringAfter("Waktu Selesai ").trim()

                dateFormatId.parse(dateTimeSubstring)
            } catch (e: Exception) {
                // Handle parsing exceptions if necessary
                Log.d("DATE RECOGNITION","Error parsing date for input: $dateString")
                null
            }
        }

        fun onSuccessDate(parsedDate: Date) {
            val formattedDate = submitFormat.format(parsedDate)
            userSelectedDate = formattedDate
            binding.tvDatePicker.text = userSelectedDate

            binding.successDate.visibility = View.VISIBLE
            binding.failDate.visibility = View.GONE

        }

        var parsedDate = parseDateEn(cleanedString)

        if (parsedDate != null) {
            Log.d("DATE RECOGNITION","Original String: $cleanedString, Parsed Date: $parsedDate")
            onSuccessDate(parsedDate)

        } else {
            parsedDate = parseDateId(cleanedString)

            if (parsedDate != null) {
                Log.d("DATE RECOGNITION","Original String: $cleanedString, Parsed Date: $parsedDate")
                onSuccessDate(parsedDate)

            } else {
                parsedDate = parseDateShopeePayPattern(cleanedString)

                if (parsedDate != null) {
                    Log.d("DATE RECOGNITION","Original String: $cleanedString, Parsed Date: $parsedDate")
                    onSuccessDate(parsedDate)

                } else {
                    Log.d("DATE RECOGNITION","Unable to parse the date.")

                }
            }
        }
    }

    private fun submitPost() {
        val title = binding.fieldTitle.text.toString()
        val body = binding.fieldBody.text.toString()
        val provider = binding.provider.text.toString()
        val date = binding.tvDatePicker.text.toString()

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
                                    writeNewPost(userId, title, body.toInt(), provider, date, task.result.toString())
                                    Log.d(TAG, task.result.toString())
                                } else {
                                    // Handle failures
                                    Log.e(TAG, "error")
                                }
                            }
                        } else {
                            writeNewPost(userId, title, body.toInt(), provider, date, "")
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
        binding.animationScan.visibility = View.GONE
        binding.tvRekognisi.visibility = View.GONE
        binding.prograss.visibility = View.VISIBLE
        binding.successNominal.visibility = View.GONE
        binding.failNominal.visibility = View.GONE

    }

    private fun onSuccess() {
        binding.animationScan.visibility = View.GONE
        binding.tvRekognisi.visibility = View.GONE
        binding.prograss.visibility = View.GONE
        binding.successNominal.visibility = View.VISIBLE
        binding.failNominal.visibility = View.GONE
    }

    private fun onFailed() {
        binding.animationScan.visibility = View.GONE
        binding.tvRekognisi.visibility = View.GONE
        binding.prograss.visibility = View.GONE
        binding.successNominal.visibility = View.GONE
        binding.failNominal.visibility = View.VISIBLE
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(), {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                userSelectedDate = formattedDate
                binding.tvDatePicker.text = userSelectedDate

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    companion object {
        private const val TAG = "NewPostFragment"
        private const val REQUIRED = "Required"
    }
}
