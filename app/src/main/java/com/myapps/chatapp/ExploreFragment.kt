package com.myapps.chatapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.database.*
import com.myapps.chatapp.adapters.GroupsAdapter
import com.myapps.chatapp.entidades.Group
import com.myapps.chatapp.entidades.Mensaje
import kotlinx.android.synthetic.main.fragment_explore.view.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ExploreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private val database = FirebaseDatabase.getInstance()
    private lateinit var ref: DatabaseReference

    lateinit var adLoader: AdLoader
    var nativeAds: ArrayList<UnifiedNativeAd> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_explore, container, false)

        groups = arrayListOf()
        addingGroupsFromDatabaseAutomatically()

        //loadNativeAds(root)

        makeAdapter(root)


        return root
    }

    private fun loadNativeAds(root: View) {
        adLoader = AdLoader.Builder(root.context, "ca-app-pub-3940256099942544/2247696110")
            .forUnifiedNativeAd { ad : UnifiedNativeAd ->
                // Show the ad.
                nativeAds.add(ad)
                if (adLoader.isLoading) {
                    // The AdLoader is still loading ads.
                    // Expect more adLoaded or onAdFailedToLoad callbacks.
                } else {
                    // The AdLoader has finished loading ads.
                    insertAdsInMenu()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    // Handle the failure by logging, altering the UI, and so on.
                    if (!adLoader.isLoading)
                        insertAdsInMenu()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build())
            .build()
        adLoader.loadAds(AdRequest.Builder().build(), 3)
    }

    private fun insertAdsInMenu() {
        val offset = groups.size / (nativeAds.size + 1)
        var index = 0
        for (ad in nativeAds) {
            //groups.add(index, ad)
            index += offset
        }
    }

    private fun makeAdapter(root: View) {
        adapter = GroupsAdapter(root.context, groups, 1)
        root.allGroupsListView.adapter = adapter
    }

    private fun addingGroupsFromDatabaseAutomatically() {
        database.getReference("All groups").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (!MainActivity.GROUPS_ID.contains(dataSnapshot.key)) {
                    val g = getGroupFromDatabase(dataSnapshot)
                    groups.add(g)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    private fun getGroupFromDatabase(dataSnapshot: DataSnapshot): Group {
        val id = dataSnapshot.child("id").getValue(String::class.java).toString()
        val name = dataSnapshot.child("name").getValue(String::class.java).toString()
        val creatorId = dataSnapshot.child("creatorId").getValue(String::class.java).toString()
        val date = dataSnapshot.child("date").getValue(String::class.java).toString()
        val foto = dataSnapshot.child("foto").getValue(String::class.java).toString()
        val n = dataSnapshot.child("numMembers").getValue(Int::class.java).toString().toInt()
        val maxNumMembers = dataSnapshot.child("maxNumMembers").getValue(Int::class.java).toString().toInt()
        val privateSettings = dataSnapshot.child("privateSettings").getValue(Boolean::class.java).toString().toBoolean()
        val onlyCreatorSendMessages = dataSnapshot.child("onlyCreatorSendMessages").getValue(Boolean::class.java).toString().toBoolean()
        val g = Group(id, name, creatorId, date, foto, n, maxNumMembers, privateSettings, onlyCreatorSendMessages,
            arrayListOf(), arrayListOf())
        for (ds in dataSnapshot.child("messages").children) {
            val m: Mensaje? = ds.getValue(Mensaje::class.java)
            g.messages.add(m!!)
        }
        for (ds in dataSnapshot.child("images").children) {
            val i: String = ds.getValue(String::class.java).toString()
            g.images.add(i)
        }
        return g
    }



    companion object {
        lateinit var adapter: GroupsAdapter
        var groups: ArrayList<Group> = arrayListOf()

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExploreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}