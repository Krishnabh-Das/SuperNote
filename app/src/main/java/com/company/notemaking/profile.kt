package com.company.notemaking

import android.R
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.company.notemaking.databinding.ActivityProfileBinding
import com.company.notemaking.utilities.Utility
import com.google.android.material.internal.ViewUtils.dpToPx
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlin.math.roundToInt


class profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    var size = 0
    var list = mutableListOf<String>("my_notes","Friends")
    lateinit var launcher:ActivityResultLauncher<String>
    val storage = FirebaseStorage.getInstance()
    var name_retrieve=""
    var bio_retrieve = ""

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = auth.currentUser?.email.toString()
        binding.userEmail.setText(email)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) // Add this flag
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }

        setTotalNotesCount()
        setProfile()

        // User UPLOADS IMAGE -> Stored in STORAGE
        launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
            if (result != null) {

                binding.progressBar4.visibility=View.VISIBLE

                val reference = storage.getReference().child("profile").child(email)

                reference.putFile(result). addOnSuccessListener {taskSnapshot ->
                    reference.downloadUrl.addOnSuccessListener { uri ->

                        // Uploading the URL in the DATABASE
                        val data = hashMapOf("downloadUrl" to uri.toString())

                        FirebaseAuth.getInstance().currentUser?.let {
                            FirebaseFirestore.getInstance().collection("Notes")
                                .document(it.uid).collection("profile").document("profile_image").set(data).addOnSuccessListener {
                                    binding.progressBar4.visibility = View.GONE
                                    binding.dpImage.setImageURI(result)
                                    Toast.makeText(this, "Uploaded", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                }
            }
        }

        binding.dpImage.setOnClickListener {
            launcher.launch("image/*")
        }


        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this@profile, loginPage::class.java))
            finish()
        }

        binding.profileBoard.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(com.company.notemaking.R.layout.profile_dialog, null)
            builder.setView(dialogView)

            val editTextName: EditText = dialogView.findViewById(com.company.notemaking.R.id.editTextName)
            val editTextBio: EditText = dialogView.findViewById(com.company.notemaking.R.id.editTextBio)
            val buttonSave: Button = dialogView.findViewById(com.company.notemaking.R.id.buttonSave)

            editTextName.setText(name_retrieve)
            editTextBio.setText(bio_retrieve)

            val alertDialog = builder.create()

            buttonSave.setOnClickListener {
                val name = editTextName.text.toString()
                val bio = editTextBio.text.toString()

                // Update UI or process the name and bio data as needed
                // For example, you can set text in your binding:
                binding.name.text = name
                binding.bio.text = bio

                val data = hashMapOf(
                    "name" to name,
                    "bio" to bio
                )

                FirebaseFirestore.getInstance().collection("Notes").document(auth.currentUser!!.uid).collection("profile").document("name_and_bio")
                    .set(data)
                name_retrieve+=name
                bio_retrieve+=bio

                // Close the AlertDialog
                alertDialog.dismiss()
            }

            alertDialog.show()
        }
    }

    private fun setProfile() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val profileImageRef = FirebaseFirestore.getInstance()
                .collection("Notes")
                .document(user.uid)
                .collection("profile")
                .document("profile_image")

            profileImageRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val uriString = documentSnapshot.getString("downloadUrl")

                    if (!uriString.isNullOrEmpty()) {
                        Picasso.get()
                            .load(uriString)
                            .into(binding.dpImage, object : Callback {
                                override fun onSuccess() {
                                    // Image successfully loaded
                                    binding.progressBar4.visibility = View.GONE
                                }

                                override fun onError(e: Exception?) {
                                    // Handle error if image loading fails
                                    Log.d("ImageLoadError", "Failed to load image: ${e?.message}")
                                    binding.progressBar4.visibility = View.GONE
                                }
                            })
                    }
                } else {
                    Log.d("Document not Found", "")
                    binding.progressBar4.visibility = View.GONE
                }
            }.addOnFailureListener { exception ->
                // Handle errors here
                Log.d("ImageLoadError", "Failed to load image: ${exception.message}")
            }
        }

        FirebaseFirestore.getInstance()
            .collection("Notes")
            .document(auth.currentUser!!.uid)
            .collection("profile")
            .document("name_and_bio")
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    val data = snapshot.data
                    for ((key, value) in data.orEmpty()) {
                        if (key == "name") {
                            name_retrieve = value.toString()
                            binding.name.text = name_retrieve
                        }
                        if( key == "bio"){
                            bio_retrieve = value.toString()
                            binding.bio.text = bio_retrieve
                            break
                        }
                    }
                }
            }
    }

    private fun setTotalNotesCount() {

        val buttonsCollection = auth.currentUser?.let {
            FirebaseFirestore.getInstance().collection("Notes")
                .document(it.uid)
                .collection("buttons")
        }

        // Move the loop and Firestore query inside the addOnSuccessListener callback
        buttonsCollection?.get()?.addOnSuccessListener { querySnapshot ->
            val buttonNames = querySnapshot.documents.map { it.id }
            // Do something with the button names (IDs) as strings
            for (buttonName in buttonNames.reversed()) {
                list.add(buttonName)
            }

            // Now that the list is fully populated, you can use it here
            for (tabName in list) {
                Log.d("list", tabName)
                Utility.getCollectionReferenceForNotes(tabName).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val totalNotes = task.result.size()
                        size += totalNotes
                        binding.totalNotes.setText(size.toString())
                        Log.d("Current Size", size.toString())
                    } else {
                        Log.d("Tab Not Found", task.exception.toString())
                    }
                }
            }
        }?.addOnFailureListener { e ->
            // Handle errors here
            Log.d("Error retrieving names from Firestore", " ${e.message}")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}