package com.company.notemaking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.company.notemaking.databinding.ActivityLoginPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class loginPage : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPageBinding
    val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener {
            loginUser()
        }



        binding.registerTextView.setOnClickListener {
            val i = Intent(this@loginPage,register::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun loginUser() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        if(!validate(email, password)){
            return
        }

        loginAccountInFirebase(email, password)
    }

    private fun loginAccountInFirebase(email: String, password: String) {
        changeInProgress(true)

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isComplete){
                changeInProgress(false)

                if(task.isSuccessful){

                    if(firebaseAuth.currentUser?.isEmailVerified == true) {
                        Toast.makeText(this@loginPage, "Welcome Back!", Toast.LENGTH_LONG).show()

                        val i = Intent(this@loginPage, MainActivity::class.java)
                        i.putExtra("first", true)
                        val email = FirebaseAuth.getInstance().currentUser?.email
                        val uid = FirebaseAuth.getInstance().currentUser?.uid

                        if (email != null) {
                            val data = hashMapOf(
                                email to uid.toString()
                            )

                            val documentRef1 = FirebaseFirestore.getInstance().collection("Notes").document("email_2_user")

                            documentRef1.set(data, SetOptions.merge()) // Use SetOptions.merge() to merge data instead of overwriting it

                            var token = "" // Declare token here

                            FirebaseMessaging.getInstance().token
                                .addOnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        Log.e("Token Details", "Token Failed to Receive")
                                        return@addOnCompleteListener
                                    }

                                    // Token retrieval was successful
                                    token = task.result.toString()
                                    Log.e("TOKEN", token)

                                    // Now that you have the token, you can update Firestore
                                    val data1 = hashMapOf(
                                        email.lowercase() to token
                                    )

                                    val documentRef2 = FirebaseFirestore.getInstance().collection("Notes").document("email_2_token")
                                    documentRef2.set(data1, SetOptions.merge())
                                }


                        } else {
                            Log.e("Error", "Email is null")
                        }

                        startActivity(i)
                        finish()

                    }else{
                        Toast.makeText(this@loginPage, "Please Verify Your Email", Toast.LENGTH_SHORT).show()
                        firebaseAuth.currentUser?.sendEmailVerification()
                    }

                }else{
                    Toast.makeText(this@loginPage, task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                }

            }
        }

    }

    private fun changeInProgress(b: Boolean) {
        if(b){
            binding.loginBtn.visibility = View.GONE
            binding.loginProgressBar.visibility = View.VISIBLE
        }else{
            binding.loginBtn.visibility = View.VISIBLE
            binding.loginProgressBar.visibility = View.GONE
        }
    }
    private fun validate(email: String, password: String) : Boolean{
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.email.error = "Invalid Email Id"
            return false
        }
        if(password.length<6){
            binding.password.error = "Too short"
            return false
        }
        return true
    }
}