package edu.ucsb.cs.cs184.rhiannaso.spoilalert.ui.home

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import clarifai2.api.ClarifaiBuilder
import clarifai2.dto.input.ClarifaiImage
import clarifai2.dto.input.ClarifaiInput
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.LandingActivity
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.NotificationPublisher
import edu.ucsb.cs.cs184.rhiannaso.spoilalert.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var signout_button: Button
    private val RequestCameraID = 123
    val CAMERA_REQUEST_CODE = 0
    lateinit var imageFilePath : String
    lateinit var bitmapImage : Bitmap
    lateinit var fileNameCleanAgain : String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

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
        uploadTask.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUrl = task.result
                Log.i("UPLOAD FILE", "$downloadUrl")
            } else{
                Log.i("UPLOAD FILE", "Something went wrong")
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

        val imageFile = createImageFile()

        val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (callCameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            val imageUri = Uri.fromFile(imageFile);
            fileNameCleanAgain = sanitiseKey(imageUri.toString())

            // Write a message to the database
            val database = Firebase.database
            val dbRef = database.getReference("files")
            val myRef = dbRef.child(fileNameCleanAgain)
            myRef.setValue(imageUri.toString())

            uploadFile("$fileNameCleanAgain.jpg", imageUri)

            callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timestamp + "_"
        val storageDirectory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (!storageDirectory?.exists()!!) {
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
            val foodView = requireActivity().findViewById<AutoCompleteTextView>(R.id.editItem)
            foodView.hint = getString(R.string.wait_msg)
            bitmapImage = setScaledBitmap()
            val baos = ByteArrayOutputStream()
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.getReference()
            val fileName = UUID.randomUUID().toString()
            val myRef_temp = storageRef.child("${fileName}.jpg")

            var uploadTask = myRef_temp.putBytes(data)
            uploadTask.addOnFailureListener {
                Toast.makeText(context, "Something went wrong on upload", Toast.LENGTH_LONG).show()
                // Handle unsuccessful uploads
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                myRef_temp.downloadUrl.addOnSuccessListener{ url ->
                    Log.i("Activity Result", "$url")
                    checkWithClarifai(url.toString())
                    val database = Firebase.database
                    val dbRef = database.getReference("files").child(fileNameCleanAgain)
                    dbRef.removeValue()
                    val photoRef: StorageReference = storage.getReferenceFromUrl(url.toString())
                    photoRef.delete()
                }
            }

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
            )
            .executeSync()
            .get();

        val data = result.first().data()
        val foodName = data[0].name()
        Log.i("Food name", foodName.toString())
        var successMsg = Toast.makeText(context, "Item successfully processed!", Toast.LENGTH_LONG)
        successMsg.setGravity(Gravity.CENTER, 0, 0)
        successMsg.show()

        val foodView = requireActivity().findViewById<AutoCompleteTextView>(R.id.editItem)
        foodView.setText(foodName)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val cam_btn = requireActivity().findViewById<ImageView>(R.id.cam_btn)
        cam_btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                            requireActivity() as Context,
                            Manifest.permission.CAMERA
                    )
                ==	PackageManager.PERMISSION_GRANTED)	{
                Log.i("PERM", "Have permission already")
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
                    Toast.makeText(context, "Please include item and quantity", Toast.LENGTH_LONG).show()
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
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("iid").setValue(dataSnapshot.child(text).child("iid").value.toString())
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("eid").setValue(uuid.toString())
                            // add item expiration date (current time + shelf life) to users item log
                            var shelfLife =
                                    dataSnapshot.child(text).child("shelf_life").value.toString()
                                            .toInt()
                            var expiration = viewModel.calculateExpiration(shelfLife)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("expiration_date").setValue(expiration)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("quantity").setValue(quantity)
                            //Id used for notification and requestCode
                            val nid = setNotificationTime(text, expiration)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("nid").setValue(nid)
                            myRef_users.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("items").child(uuid.toString()).child("notif_index").setValue("0")

                            text_view.setText(null)
                            quantity_view.setText(null)

                            var msg = "$quantity $text(s) added to your fridge!"

                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            val foodView = requireActivity().findViewById<AutoCompleteTextView>(R.id.editItem)
                            foodView.hint = getString(R.string.log_hint)
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

    //sets an alarm to send a notification
    private fun setNotificationTime(itemName: String, date: String) : Int {
        val format = SimpleDateFormat("EEEE, MM/dd/yyyy 'at' h:mm a")
        var d: Date = Date()
        try {
            d = format.parse(date)
        } catch (e : ParseException) {
            Log.d("HomeFragment", e.message!!)
        }

        val c = Calendar.getInstance()
        //nid will be used for both notification-id along with requestCode
        val nid = Integer.parseInt(SimpleDateFormat("ddHHmmssSS", Locale.US).format(c.time))

        //create notification
        val n = NotificationCompat.Builder(requireActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_kitchen_24px)
                .setContentTitle("$itemName expiring soon!")
                .setContentText("$itemName expiring within a day! Try to eat them if you can!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        //create intent and pendingIntent
        val notificationIntent = Intent(activity, NotificationPublisher::class.java)
        notificationIntent.putExtra("notification-id", nid)
        notificationIntent.putExtra("notification", n.build())
        val pIntent = PendingIntent.getBroadcast(activity, nid, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //set alarm
        c.time = d
//        c.add(Calendar.SECOND, 5)
        val futureInMillis = c.timeInMillis
        val aM = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        aM.set(AlarmManager.RTC_WAKEUP, futureInMillis, pIntent)

        return nid
    }
}