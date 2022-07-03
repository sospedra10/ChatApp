package com.myapps.chatapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {
    private val PHOTO_PERFIL: Int = 2

    private val database = FirebaseDatabase.getInstance()
    private lateinit var databaseReference: DatabaseReference
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageReference: StorageReference = storage.getReference("imagenes_foto_perfil")
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_settings)

        //val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        //settingsMainLayout.startAnimation(bottomAnim)

        nameSettingsTv.text = MainActivity.USER_NAME

        if (MainActivity.FOTO_PERFIL != "")
            Glide.with(this@SettingsActivity).load(MainActivity.FOTO_PERFIL).into(fotoPerfilSettings)

        buttons()
    }

    // cambiamos foto perfil
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_PERFIL && resultCode == RESULT_OK) {
            val u: Uri? = data?.data
            storageReference = storage.getReference("imagenes_foto_perfil") //imagenes_chat
            MainActivity.FOTO_PERFIL = u.toString()
            Glide.with(this).load(u.toString()).into(fotoPerfilSettings)

            // actualizamos foto perfil en firebase
            updateFotoPerfil()

            val fotoReferencia = storageReference.child(u!!.lastPathSegment!!)
            fotoReferencia.putFile(u).addOnSuccessListener(this@SettingsActivity) { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    val uri = task.result!!
                }
            }
        }
    }

    private fun updateFotoPerfil() {
        databaseReference = database.getReference("Usuarios/" + MainActivity.ID + "/fotoPerfil")
        databaseReference.setValue(MainActivity.FOTO_PERFIL)
    }

    private fun updateName() {
        databaseReference = database.getReference("Usuarios/" + MainActivity.ID + "/name")
        databaseReference.setValue(MainActivity.USER_NAME)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buttons() {
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down)

        closeSessionBtn.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> closeSessionBtn.startAnimation(scaleUp)
                MotionEvent.ACTION_UP -> {
                    closeSessionBtn.startAnimation(scaleDown)
                    Handler().postDelayed( {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }, 100)
                }
            }
            true
        }


        fotoPerfilSettings.setOnClickListener {
            val i = Intent(Intent.ACTION_GET_CONTENT).setType("image/jpeg")
            i.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(i, "Selecciona una foto"), PHOTO_PERFIL)
        }

        nameSettingsTv.setOnClickListener {
            nameSettingsTv.visibility = View.GONE
            nameSettingsEditText.visibility = View.VISIBLE
            nameSettingsEditText.setText(MainActivity.USER_NAME)
            nameSettingsEditText.requestFocus()
            nameSettingsEditText.showKeyboard()
        }


        nameSettingsEditText.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                MainActivity.USER_NAME = nameSettingsEditText.text.toString()
                updateName()
                nameSettingsEditText.visibility = View.GONE
                nameSettingsTv.visibility = View.VISIBLE
                nameSettingsTv.text = MainActivity.USER_NAME
                nameSettingsEditText.hideKeyboard()
                true
            }
            else false
        })

        settingsBackBtn.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> settingsBackBtn.startAnimation(scaleUp)
                MotionEvent.ACTION_UP -> {
                    settingsBackBtn.startAnimation(scaleDown)
                    val leftAnim = AnimationUtils.loadAnimation(this, R.anim.leave_right_animation)
                    settingsMainLayout.startAnimation(leftAnim)
                    Handler().postDelayed( {
                        settingsMainLayout.visibility = View.GONE
                        startActivity(Intent(this, MainActivity::class.java))
                    }, 200)
                }
            }
            true
        }



    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}