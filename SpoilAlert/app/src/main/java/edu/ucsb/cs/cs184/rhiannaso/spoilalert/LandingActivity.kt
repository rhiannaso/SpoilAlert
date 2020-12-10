package edu.ucsb.cs.cs184.rhiannaso.spoilalert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LandingActivity : AppCompatActivity() {
    var RC_SIGN_IN = 123
    private lateinit var signin_button : SignInButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.landing_activity)

        signin_button = findViewById(R.id.google_button)
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // signin button intent
        signin_button.setOnClickListener {
            Log.d("google signin", "signin button clicked")
            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Log.d("signin success", "signin succeeded, got user instance")
                signin_button.visibility = View.INVISIBLE
                var login_tv = findViewById<TextView>(R.id.login_textview)
                login_tv.setText("Hello " + FirebaseAuth.getInstance().currentUser?.displayName.toString())

                // add user to Firebase users table
                val database = Firebase.database
                val myRef = database.getReference("users")
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if (dataSnapshot.hasChild(FirebaseAuth.getInstance().currentUser?.uid.toString())) {
                            Log.d(
                                "user",
                                FirebaseAuth.getInstance().currentUser?.uid.toString() + " already exists in users table"
                            )
                        } else {
                            Log.d(
                                "user",
                                FirebaseAuth.getInstance().currentUser?.uid.toString() + " does not exist in users table, adding now"
                            )
                            myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                            myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .child(
                                    "name"
                                ).setValue(
                                FirebaseAuth.getInstance().currentUser?.displayName.toString()
                            )
                        }
                        val resultIntent = Intent()
                        resultIntent.putExtra("user_id", FirebaseAuth.getInstance().currentUser?.uid.toString())
                        setResult(RESULT_OK, resultIntent)
                        finish()
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