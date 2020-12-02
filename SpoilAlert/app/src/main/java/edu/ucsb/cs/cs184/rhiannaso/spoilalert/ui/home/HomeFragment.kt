package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    var RC_SIGN_IN = 123
    var signed_in = false
    private lateinit var signin_button : SignInButton
    private lateinit var signout_button : Button


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // val textView: TextView = root.findViewById(R.id.text_home)
        // homeViewModel.text.observe(viewLifecycleOwner, Observer {
        // textView.text = it
        // })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        signin_button = requireActivity().findViewById(R.id.google_button)
        signout_button = requireActivity().findViewById(R.id.signout_button)

        // logic for displaying signin/signout button
        if (signed_in) {
            signout_button.visibility = View.VISIBLE
            signin_button.visibility = View.INVISIBLE
        }
        else {
            signin_button.visibility = View.VISIBLE
            signout_button.visibility = View.INVISIBLE
        }

        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build())

        // signin button intent
        signin_button.setOnClickListener {
            Log.d("google signin", "signin button clicked")
            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN)
        }

        // signout button intent
        signout_button.setOnClickListener {
            AuthUI.getInstance()
                .signOut(activity as Context)
                .addOnCompleteListener {
                    // ...
                }
            signed_in = false
            var login_tv = requireActivity().findViewById<TextView>(R.id.login_textview)
            login_tv.setText("")
            signout_button.visibility = View.INVISIBLE
            signin_button.visibility = View.VISIBLE
        }

        var button = requireActivity().findViewById<Button>(R.id.submit_button)
        // onClick for submit item button
        button.setOnClickListener {
            // if signed_in, then check if item is in the db item table
            if (signed_in) {
                var text_view =
                    requireActivity().findViewById<EditText>(R.id.editTextTextPersonName)
                var text = text_view.getText().toString()
                Log.d("input text", text)

                val database = Firebase.database
                val myRef_items = database.getReference("items")
                val myRef_users = database.getReference("users")

                myRef_items.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if (dataSnapshot.hasChild(text)) {
                            Log.d("input",
                                dataSnapshot.child(text)
                                    .child("name").value.toString() + "for user: " + FirebaseAuth.getInstance().uid.toString()
                            )
                            // generate new uid for entry if the item is in the db items table
                            var uuid = UUID.randomUUID()
                            Log.d("UUID", uuid.toString())
                            // add item name to users item log
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("name").setValue(text)
                            // add item iid to users item log
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("iid").setValue(dataSnapshot.child(text).child("iid").value.toString())
                            // add item shelf life to users item log
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("expiration_date").setValue(dataSnapshot.child(text).child("shelf_life").value.toString())
                        } else {
                            Log.d("input", "input item does not exist in items table")
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("In surfaceCreated", "Failed to read value.", error.toException())
                    }
                })
            }
            else {
                // user has not logged in yet
                Snackbar.make(requireView(), "Must sign in to add an item", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
    }

    // signin button intent onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Log.d("signin success", "signin succeeded, got user instance")
                var login_tv = requireActivity().findViewById<TextView>(R.id.login_textview)
                login_tv.setText("Hello " + FirebaseAuth.getInstance().currentUser?.displayName.toString())
                signed_in = true
                signout_button.visibility = View.VISIBLE
                signin_button.visibility = View.INVISIBLE

                // add user to Firebase users table
                val database = Firebase.database
                val myRef = database.getReference("users")
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if (dataSnapshot.hasChild(FirebaseAuth.getInstance().currentUser?.uid.toString())) {
                            Log.d("user", FirebaseAuth.getInstance().currentUser?.uid.toString() + " already exists in users table")
                        }
                        else {
                            Log.d("user", FirebaseAuth.getInstance().currentUser?.uid.toString() + " does not exist in users table, adding now")
                            myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                            myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("name").setValue(FirebaseAuth.getInstance().currentUser?.displayName.toString())
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("In surfaceCreated", "Failed to read value.", error.toException())
                    }
                })

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }


}