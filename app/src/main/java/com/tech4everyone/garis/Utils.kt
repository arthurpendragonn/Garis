package com.tech4everyone.garis

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.icu.text.NumberFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createCustomTemplateFile(context: Context): File {
    val storage: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storage)
}

fun rupiah(number: Int): String{
    val localeID =  Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    return numberFormat.format(number).toString()
}
fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun rotateBitmapImage(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
    val matrix = Matrix()
    return if (isBackCamera) {
        matrix.postRotate(90f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver : ContentResolver = context.contentResolver
    val myFileStory = createCustomTemplateFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream : OutputStream = FileOutputStream(myFileStory)
    val buf = ByteArray(1024)
    var len : Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFileStory
}

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > 1000000)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

fun bitmapToFile(context: Context, bitmap: Bitmap): File {
    val file = File(context.getCacheDir(), "image.jpg")
    file.createNewFile();

    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
    val bitmapData = bos.toByteArray()

    val fos = FileOutputStream(file)
    fos.write(bitmapData)
    fos.flush()
    fos.close()

    return file
}


//private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
//    try {
//        val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
//        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
//        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
//        parcelFileDescriptor.close()
//        return image
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//    return null
//}