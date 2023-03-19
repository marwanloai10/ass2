@file:Suppress("DEPRECATION")

package com.example.myapplication99

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    private val TAG = "com.example.myapplication99.MainActivity"
    private val PDF_SELECTION_CODE = 1000

    private lateinit var selectPdfButton: Button
    private lateinit var downloadPdfButton: Button
    private lateinit var storageReference: StorageReference
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectPdfButton = findViewById(R.id.select_pdf_button)
        downloadPdfButton = findViewById(R.id.download_pdf_button)

        storageReference = FirebaseStorage.getInstance().getReference("pdfs")

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.setCancelable(false)

        selectPdfButton.setOnClickListener {
            selectPdfFromDevice()
        }

        downloadPdfButton.setOnClickListener {
            downloadPdf()
        }
    }

    private fun selectPdfFromDevice() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_SELECTION_CODE)
    }

    private fun uploadPdfToFirebaseStorage(pdfUri: Uri) {
        progressDialog.show()

        val fileName = System.currentTimeMillis().toString()
        val pdfStorageReference = storageReference.child("pdf_$fileName")

        pdfStorageReference.putFile(pdfUri)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "PDF uploaded successfully!", Toast.LENGTH_SHORT).show()
                downloadPdfButton.isEnabled = true
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Log.e(TAG, "Error uploading PDF", exception)
                Toast.makeText(this, "Error uploading PDF", Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressDialog.setMessage("Uploading... $progress%")
            }
    }

    private fun downloadPdf() {
        progressDialog.show()

        val fileName = "pdf_${System.currentTimeMillis()}"
        val pdfStorageReference = storageReference.child("$fileName.pdf")

        pdfStorageReference.downloadUrl
            .addOnSuccessListener { uri ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/pdf")
                startActivity(intent)
                progressDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Log.e(TAG, "Error downloading PDF", exception)
                Toast.makeText(this, "Error downloading PDF", Toast.LENGTH_SHORT).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PDF_SELECTION_CODE && resultCode == RESULT_OK && data != null) {
            val pdfUri = data.data ?: return
            uploadPdfToFirebaseStorage(pdfUri)
        }
    }
}