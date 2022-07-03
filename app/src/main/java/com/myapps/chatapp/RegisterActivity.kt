package com.myapps.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.myapps.chatapp.entidades.User
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        registerBtn.setOnClickListener {
            val email = email.text.toString()
            val name = username.text.toString()
            if (validUser(name, email)) {
                val password = password1.text.toString()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(this, "Registration succesful", Toast.LENGTH_SHORT).show()
                            val currentUser: FirebaseUser? = auth.currentUser
                            val usuario = User(currentUser!!.uid, name, email, "", false)
                            val reference = database.getReference("Usuarios/" + currentUser.uid)
                            reference.setValue(usuario)

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else {
                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validUser(name: String, email: CharSequence): Boolean {
        if (isValidEmail(email) && validPassword() && validName(name))
            return true
        return false
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun validPassword(): Boolean {
        val password1: String = password1.text.toString()
        val password2: String = password2.text.toString()
        if (password1 == password2 && password1.length in 6..16)
            return true
        return false
    }

    private fun validName(name: String): Boolean {
        return name.isNotEmpty()
    }
}