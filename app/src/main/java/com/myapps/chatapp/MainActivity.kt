package com.myapps.chatapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.myapps.chatapp.entidades.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*


class MainActivity : AppCompatActivity() {
    companion object {
        var USER_NAME: String = ""
        var ID: String = ""
        var EMAIL: String = ""
        var FOTO_PERFIL: String = ""
        var SPECIAL: Boolean = false
        var GROUPS_ID: ArrayList<String> = arrayListOf()

        var users: ArrayList<User> = arrayListOf()

    }

    private val database = FirebaseDatabase.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        mainActivityMainLayout.startAnimation(bottomAnim)

        val purchaseUpdateListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
            }
        val billingClient = BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    //billingClient.querySkuDetailsAsync()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.adBannerMainActivity, BannerFragment())
            commit()
        }

        getData()

        viewPager()

        verifyStoragePermissions(this)

    }

    override fun onStart() {
        super.onStart()
        //getUsersData()
    }

    private fun viewPager() {
        Thread.sleep(200)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        viewPager.setPageTransformer(true, ZoomOutPageTransformer)

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }


    private fun getData() {
        getUserData()
        getUsersData()
    }

    private fun getUsersData() {

        Thread {
            users = arrayListOf()
            val ref = database.getReference("Usuarios")
            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val u = getDataUser(dataSnapshot)
                    users.add(u)
                }

                @RequiresApi(Build.VERSION_CODES.N)
                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    val id = dataSnapshot.child("id").getValue(String()::class.java).toString()
                    users.removeIf { it.id == id }
                    val u = getDataUser(dataSnapshot)
                    users.add(u)

                }
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }.start()


    }

    private fun getDataUser(dataSnapshot: DataSnapshot): User {
        val name = dataSnapshot.child("name").getValue(String()::class.java).toString()
        val fotoPerfil = dataSnapshot.child("fotoPerfil").getValue(String()::class.java).toString()
        val id = dataSnapshot.child("id").getValue(String()::class.java).toString()
        val email = dataSnapshot.child("email").getValue(String()::class.java).toString()
        val special = dataSnapshot.child("special").getValue(Boolean::class.java).toString().toBoolean()
        val u = User(id, name, email, fotoPerfil, special, arrayListOf())
        for (ds in dataSnapshot.child("groupsId").children) {
            val i: String = ds.getValue(String::class.java).toString()
            u.groupsId.add(i)
        }
        return u
    }


    private fun getUserData() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val reference = database.getReference("Usuarios/" + currentUser.uid)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //val user: User? = dataSnapshot.getValue(User()::class.java)
                    USER_NAME = dataSnapshot.child("name").getValue(String()::class.java).toString()
                    FOTO_PERFIL = dataSnapshot.child("fotoPerfil").getValue(String()::class.java).toString()
                    ID = dataSnapshot.child("id").getValue(String()::class.java).toString()
                    EMAIL = dataSnapshot.child("email").getValue(String()::class.java).toString()
                    SPECIAL = dataSnapshot.child("special").getValue(Boolean::class.java).toString().toBoolean()
                    GROUPS_ID = arrayListOf()
                    for (ds in dataSnapshot.child("groupsId").children) {
                        val i: String = ds.getValue(String::class.java).toString()
                        GROUPS_ID.add(i)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }


    private fun verifyStoragePermissions(activity: Activity?): Boolean {
        val PERMISSIONS_STORAGE = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val REQUEST_EXTERNAL_STORAGE = 1
        val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            false
        }
        else true
    }

}


private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    companion object : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA +
                                (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }

    }

    override fun transformPage(page: View, position: Float) {
        TODO("Not yet implemented")
    }


}