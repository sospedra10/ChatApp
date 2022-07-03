package com.myapps.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_welcome)

        animations()

        checkLoggedIn()
    }

    private fun animations() {
        val topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.welcome_bottom_animation)
        val middleAnimation = AnimationUtils.loadAnimation(this, R.anim.welcome_left_animation)
        firstLine.animation = topAnimation
        secondLine.animation = topAnimation
        thirdLine.animation = topAnimation
        fourthLine.animation = topAnimation
        fifthLine.animation = topAnimation
        sixthLine.animation = topAnimation
        titleTextView.animation = middleAnimation
        textView2.animation = bottomAnimation
    }

    private fun checkLoggedIn() {
        val pastTime = System.currentTimeMillis()

        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) { // is signed in (it doesnt mean he has an account (could have been deleted))
            val reference = database.getReference("Usuarios/" + currentUser.uid)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: String? = dataSnapshot.child("id").getValue(String()::class.java)
                    if (user != null) { // he has an account
                        val actualTime = System.currentTimeMillis()
                        val passed = actualTime - pastTime
                        if (passed < 1500) {
                            Handler().postDelayed( {
                                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                            },1500 - passed)
                        }
                        else
                            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                    } else {
                        startActivity(Intent(this@WelcomeActivity, RegisterActivity::class.java))
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@WelcomeActivity, "cancelled", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else {
            startActivity(Intent(this, LoginActivity::class.java))

        }
    }
}