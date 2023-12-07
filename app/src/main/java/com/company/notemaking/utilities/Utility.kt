package com.company.notemaking.utilities

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.company.notemaking.models.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore


object Utility {

    fun getCollectionReferenceForNotes(name : String): CollectionReference {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("Notes")
                .document(currentUser.uid).collection(name)
        } else {
            FirebaseFirestore.getInstance().collection("EmptyCollection")
        }
    }

    fun getCollectionReferenceForAll(): CollectionReference{
        val currentUser = FirebaseAuth.getInstance().currentUser
        return if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("Notes")
                .document("All").collection("All")
        } else {
            FirebaseFirestore.getInstance().collection("EmptyCollection")
        }
    }
    fun sendNoteIdByEmail(email: String, context: Context, note: Note, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // If the user is signed in, proceed with Firestore query
        if (currentUser != null) {
            val documentRef = FirebaseFirestore.getInstance().collection("Notes")
                .document("email_2_user")

            documentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Extracting the Fields
                        val userData = documentSnapshot.data

                        // Initialize userId as null
                        var userId: String? = null

                        // Iterate through the fields and find the matching email
                        for ((key, value) in userData.orEmpty()) {
                            if (key == email) {
                                userId = value.toString()
                                Log.d("userId", userId)
                                break  // Exit the loop once a match is found
                            }
                        }

                        // Check if userId is not null before setting the note
                        if (userId != null) {
                            FirebaseFirestore.getInstance().collection("Notes")
                                .document(userId).collection("Friends").document().set(note)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Notes Shared to $email", Toast.LENGTH_SHORT).show()
                                        callback(true) // Email was found
                                    } else {
                                        Toast.makeText(context, "Cannot share", Toast.LENGTH_LONG).show()
                                        callback(false) // Email was not found
                                    }
                                }
                        } else {
                            Log.d("userId", "No such email Found: $email")
                            Toast.makeText(context, "No such email Found: $email", Toast.LENGTH_LONG).show()
                            callback(false) // Email was not found
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Failed to retrieve", exception.localizedMessage.toString())
                    callback(false) // Error occurred, email was not found
                }
        } else {
            // User is not signed in
            callback(false) // User is not signed in, email was not found
        }
    }


}
