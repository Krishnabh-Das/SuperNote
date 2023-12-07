package com.company.notemaking.onBoard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.company.notemaking.MainActivity
import com.company.notemaking.R
import com.company.notemaking.loginPage
import com.google.firebase.auth.FirebaseAuth


class onBoardingFragment3 : Fragment() {
    val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_on_boarding3, container, false) as ViewGroup
        root.findViewById<Button>(R.id.next).setOnClickListener {
            startActivity(Intent(activity, loginPage::class.java))
            activity?.finish()
        }
        return root
    }
}