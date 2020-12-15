package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.home

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import clarifai2.api.ClarifaiBuilder
import clarifai2.dto.input.ClarifaiImage
import clarifai2.dto.input.ClarifaiInput
import clarifai2.dto.model.output.ClarifaiOutput
import clarifai2.dto.prediction.Concept
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.LandingActivity
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var signout_button: Button
    private val RequestCameraID = 123
    val CAMERA_REQUEST_CODE = 0
    val fileName: String = UUID.randomUUID().toString()
    //val photoFileName: String = "photo0000.jpg"
    lateinit var imageFilePath : String
    lateinit var bitmapImage : Bitmap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        /*val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        return root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.i("PERM", "Checking perm")
        when (requestCode)	{
            RequestCameraID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PERM", "Permission granted")
                }
            }
        }
    }

    fun uploadFile(key: String, fileUri: Uri) {
        val storage = Firebase.storage
        val storageRef = storage.getReference(key)

        val uploadTask = storageRef.putFile(fileUri)
        Log.i("UPLOAD", "Hello")
        uploadTask.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUrl = task.result
                Log.i("UPLOAD", "$downloadUrl")
                Toast.makeText(context, downloadUrl.toString(), Toast.LENGTH_LONG).show()

            } else{
                Log.i("UPLOAD", "whoops")
            }
        }
    }

    fun sanitiseKey(fileName: String) : String {
        val fileNameClean = fileName.split("/");
        val fileNameCleanAgain = fileNameClean[fileNameClean.size - 1].replace(".", "").replace(
            "jpg",
            ""
        )

        return fileNameCleanAgain
    }

    private fun takePicture() {
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        //val imageFile = File.createTempFile(fileName, ".jpg")
        val imageFile = createImageFile()

        val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //val imageUri = Uri.fromFile(imageFile);

        // Write a message to the database
        //val imageUri = getPhotoFileUri(fileName+".jpg")
        //val imageUri = getPhotoFileUri(photoFileName)
        /*val database = Firebase.database
        val myRef = database.getReference().child("tempFile")
        myRef.setValue(imageUri.toString())
        //uploadFile(fileName + ".jpg", imageUri!!)
        uploadFile(photoFileName, imageUri!!)*/

        //callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        Log.i("CAM", "Going to take picture")
        if (callCameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            val imageUri = Uri.fromFile(imageFile);
            val fileNameCleanAgain = sanitiseKey(imageUri.toString())

            // Write a message to the database
            val database = Firebase.database
            val myRef = database.getReference(fileNameCleanAgain.toString())
            myRef.setValue(imageUri.toString())

            uploadFile(fileNameCleanAgain + ".jpg", imageUri)

            callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timestamp + "_"
        val storageDirectory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (!storageDirectory?.exists()!!) {
            // mkdirs() function used instead of mkdir() to create any parent directory that does
            // not exist.

            storageDirectory.mkdirs()
        } else {
            println("Directory exists!");
        }

        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDirectory)
        imageFilePath = imageFile.absolutePath

        return imageFile
    }

    fun setScaledBitmap(): Bitmap {
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, bmOptions)

        val scaleFactor = 1
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(imageFilePath, bmOptions)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        intent: Intent?
    ) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.i("ACTIVITY RESULT", "In here")
            //val bmp = intent?.extras?.get("data") as Bitmap

            //val takenPhotoUri = getPhotoFileUri(fileName+".jpg")
            /*val takenPhotoUri = getPhotoFileUri(photoFileName)
            val takenImage = BitmapFactory.decodeFile(takenPhotoUri!!.path)*/

            //val img = requireActivity().findViewById<ImageView>(R.id.camera_image)
            //img.setImageBitmap(takenImage)

            bitmapImage = setScaledBitmap()
            val baos = ByteArrayOutputStream()
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.getReference()
            //val myRef_temp = storageRef.child(fileName+".jpg")
            //val myRef_temp = storageRef.child(photoFileName)
            val myRef_temp = storageRef.child("food.jpg")
            //val myRef_temp = storage.getReference("food.jpg")

            var uploadTask = myRef_temp.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(context, "RIP", Toast.LENGTH_LONG).show()
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                val downloadUrl = taskSnapshot.metadata?.reference?.downloadUrl
                val dURL = myRef_temp.downloadUrl.addOnSuccessListener{
                    url ->
                    Log.i("Activity Result", "$url")
                    checkWithClarifai(url.toString())

                }
                /*Toast.makeText(context, dURL.toString(), Toast.LENGTH_LONG).show()
                Log.i("Activity Result", "$dURL")
                Log.i("Activity Result", "$myRef_temp")
                checkWithClarifai(myRef_temp.toString())*/
            }
            // Get the Uri of data
            //val file_uri = intent.data
            /*val img = requireActivity().findViewById<ImageView>(R.id.camera_image)
            img.visibility = View.VISIBLE
            img.setImageBitmap(bmp)*/

        } else {
            Log.i("ACTIVITY RESULT", "Something went wrong")
        }
    }

    fun checkWithClarifai(downloadUrl: String) {
        Log.i("CLARIFAI", "Checking with clarifai")
        val APIKey = "020d8722b0f0431aa162dea9b7bd5b9e"
        val client = ClarifaiBuilder(APIKey).buildSync()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val result = client.defaultModels.foodModel()
            .predict()
            .withInputs(
                ClarifaiInput.forImage(ClarifaiImage.of(downloadUrl))
                //ClarifaiInput.forImage(ClarifaiImage.of("https://firebasestorage.googleapis.com/v0/b/spoilalert-7c0e6.appspot.com/o/mountains.jpg?alt=media&token=4e89e757-ad7d-4b01-bd08-5ff70123f566"))
            )
            .executeSync()
            .get();

        val data = result.first().data()
        val foodName = data[0].name()
        Log.i("Food name", foodName.toString())

        val foodView = requireActivity().findViewById<AutoCompleteTextView>(R.id.editItem)
        foodView.setText(foodName)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_camera)
        fab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireActivity() as Context,
                    Manifest.permission.CAMERA
                )
                ==	PackageManager.PERMISSION_GRANTED)	{
                Log.i("PERM", "Have permission already")
                Log.i("CAM", "Can take picture")
                takePicture()
            } else {
                Log.i("PERM", "No permission yet")
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    RequestCameraID
                );
            }
        }

        signout_button = requireActivity().findViewById(R.id.signout_button)

        // signout button intent
        signout_button.setOnClickListener {
            AuthUI.getInstance()
                .signOut(activity as Context)
                .addOnCompleteListener {
                    // ...
                }
            val drawerLayout: DrawerLayout = requireActivity().findViewById(R.id.drawer_layout)
            drawerLayout.closeDrawer(Gravity.START as Int)
            val landingIntent = Intent(
                requireActivity().applicationContext,
                LandingActivity::class.java
            )
            requireActivity().startActivityForResult(landingIntent, 0)
        }

        var text_view = requireActivity().findViewById<AutoCompleteTextView>(R.id.editItem)
        val languages = resources.getStringArray(R.array.Languages)
        val adapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_list_item_1, languages
            )
        }
        text_view.setAdapter(adapter)

        var button = requireActivity().findViewById<Button>(R.id.submit_button)
        // onClick for submit item button
        button.setOnClickListener {
            var text_view = requireActivity().findViewById<EditText>(R.id.editItem)

            var quantity_view = requireActivity().findViewById<EditText>(R.id.editQuantity)
            var text = text_view.getText().toString()
            var quantity = quantity_view.getText().toString()

            if (quantity.trim().length <= 0 || text.trim().length <= 0) {
                if (quantity.trim().length <= 0 && text.trim().length <= 0)
                    Toast.makeText(context, "Please include item and quantity", Toast.LENGTH_LONG)
                        .show()
                else if (text.trim().length <= 0)
                    Toast.makeText(context, "Please include item", Toast.LENGTH_SHORT).show()
                else if (quantity.trim().length <= 0)
                    Toast.makeText(context, "Please include quantity", Toast.LENGTH_LONG).show()
            } else {
                Log.d("input text", text)
                Log.d("input quantity", quantity.toString())

                val database = Firebase.database
                val myRef_items = database.getReference("items")
                val myRef_users = database.getReference("users")

                myRef_items.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if (dataSnapshot.hasChild(text)) {
                            Log.d(
                                "input",
                                dataSnapshot.child(text)
                                    .child("name").value.toString() + "for user: " + FirebaseAuth.getInstance().uid.toString()
                            )
                            // generate new uid for entry if the item is in the db items table
                            var uuid = UUID.randomUUID()
                            Log.d("UUID", uuid.toString())
                            // add item name to users item log
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .child("items").child(uuid.toString()).child("name").setValue(text)
                            // add item iid to users item log
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .child("items").child(uuid.toString()).child("iid")
                                .setValue(dataSnapshot.child(text).child("iid").value.toString())
                            // add item expiration date (current time + shelf life) to users item log
                            var shelfLife =
                                dataSnapshot.child(text).child("shelf_life").value.toString()
                                    .toInt()
                            var expiration = viewModel.calculateExpiration(shelfLife)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .child("items").child(uuid.toString()).child("expiration_date")
                                .setValue(expiration)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .child("items").child(uuid.toString()).child("quantity")
                                .setValue(quantity)

                            text_view.setText(null)
                            quantity_view.setText(null)

                            var msg = quantity + " " + text + "(s) added to your fridge!"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

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
        }
    }
    private val CHANNEL_ID = "spoil_alert_id"
    private val notificationId = 101
    private fun sendNotification() {
        val builder = context?.let {
            NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kitchen_24px)
                .setContentTitle("7am")
                .setContentText("the usual morning line up")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        if (builder != null) {
            with(context?.let { NotificationManagerCompat.from(it) }) {
                this?.notify(notificationId, builder.build())
            }
        }
    }
}