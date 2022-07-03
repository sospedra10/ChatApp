package com.myapps.chatapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myapps.chatapp.entidades.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login )

        //checkLoggedIn()

        loginLoginBtn.setOnClickListener {
            val email: String = emailLogin.text.toString()
            if (isValidEmail(email) && validPassword()) {
                val password: String = passwordLogin.text.toString()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, OnCompleteListener<AuthResult?> { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                this,
                                "You may not have an account",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show()
            }

        }

        registerLoginBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkLoggedIn() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) { // is signed in (it doesnt mean he has an account (could have been deleted))
            val reference = database.getReference("Usuarios/" + currentUser.uid)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user: User? = dataSnapshot.getValue(User()::class.java)
                    if (user != null) { // he has an account
                        Toast.makeText(this@LoginActivity, "Usuario logeado.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun validPassword(): Boolean {
        val password: String = passwordLogin.text.toString()
        return password.length in 6..16
    }

    override fun onStart() {
        super.onStart()

    }

}