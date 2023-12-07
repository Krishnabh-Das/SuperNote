package com.company.notemaking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.company.notemaking.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createButtomBtn.setOnClickListener{
            CreateAccount()
        }

        binding.loginTextView.setOnClickListener{
            finish()
        }

    }

    private fun CreateAccount() {
        val email = binding.registerEmail.text.toString()
        val password = binding.registerPassword.text.toString()
        val confirmPassword = binding.registerConfirmPassword.text.toString()

        var isValidate = validate(email,password,confirmPassword)

        if(!isValidate) {
            return
        }

        createAccountInFirebase(email, password)
    }

    private fun createAccountInFirebase(email: String, password: String) {
        changeInProgress(true)

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){ task ->
            if(task.isComplete) {
                changeInProgress(false)
                if (task.isSuccessful == true) {
                    Toast.makeText(this@register, "Verification Mail Send to your Account!", Toast.LENGTH_LONG).show()
                    firebaseAuth.currentUser?.sendEmailVerification()

                    val intent = Intent(this@register, loginPage::class.java)
                    intent.putExtra("create", true)
                    firebaseAuth.signOut()
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@register, task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun changeInProgress(b: Boolean) {
        if(b){
            binding.createButtomBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.createButtomBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun validate(email: String, password: String, confirmPassword: String) : Boolean{
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.registerEmail.error = "Invalid Email Id"
            return false
        }
        if(password.length<6){
            binding.registerPassword.error = "Too short"
            return false
        }
        if(!password.equals(confirmPassword)){
            binding.registerConfirmPassword.error = "Password doesn't match"
            return false
        }
        return true
    }
}