package com.company.notemaking.onBoard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.company.notemaking.MainActivity
import com.company.notemaking.R
import com.company.notemaking.databinding.ActivitySplashBinding

class splash_activity : AppCompatActivity() {
    private val Num_Pages = 3
    private lateinit var viewPager: ViewPager
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter
    private lateinit var binding: ActivitySplashBinding
    val splash_time_out = 3500

    var anim: LottieAnimationView? = null
    var lottieAnimationView: LottieAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        window.navigationBarColor = Color.TRANSPARENT

        // Adding the OnBoard Pages
        viewPager = binding.pager
        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter

        // Giving Animation
        lottieAnimationView = binding.lottieId
        anim = binding.lottieId
        anim!!.playAnimation()

        val anim_for_pager = AnimationUtils.loadAnimation(this, R.anim.my_anim)
        lottieAnimationView!!.animate().translationY(-1800f).setDuration(1000).startDelay = 2000
        binding.textView.animate().translationY(1800f).setDuration(1300).startDelay = 1700

        viewPager.animation = anim_for_pager


        //Checking if NEW User or Not
        Handler().postDelayed({
            val pref = getSharedPreferences("OnBoard", Context.MODE_PRIVATE)
            val isFirstTime = pref.getBoolean("first_time", true)
            if (isFirstTime) {
                val editor: SharedPreferences.Editor = pref.edit()
                editor.putBoolean("first_time", false)
                editor.apply()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, splash_time_out.toLong())
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int {
            return Num_Pages
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> onBoardingFragment1()
                1 -> onBoardingFragment2()
                2 -> onBoardingFragment3()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}