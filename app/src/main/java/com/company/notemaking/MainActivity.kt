package com.company.notemaking

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.company.notemaking.adapter.NoteAdapter
import com.company.notemaking.databinding.ActivityMainBinding
import com.company.notemaking.models.Note
import com.company.notemaking.utilities.Utility
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), NoteAdapter.notesClickListener,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notes_adapter: NoteAdapter
    private var homeSelect = false
    lateinit var docId: String
    val Speech_Code = 105
    var current_button = "my_notes"
    var previous_button_clicked: Button? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("Create","")
        // Bottom Navigation View
        val bottomNavigationView = binding.bnv

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }


        // Firebase Cloud Messaging
        FirebaseApp.initializeApp(this)

        // User Login Check
        userLoginCheck()

        if (!areNotificationsEnabled()) {
            showNotificationPermissionDialog()
        }

        // Navigation Bar Items
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    if (this@MainActivity !is MainActivity) {
                        val i = Intent(this@MainActivity, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this@MainActivity, profile::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    homeSelect = true
                    true
                }
                else -> {
                    false
                }
            }
        }

        binding.All.setOnClickListener {
            current_button = "All"
            setButtonClick(binding.All)
            setUpAllRecyclerView()
        }

        binding.myNotes.setOnClickListener {
            current_button = "my_notes"
            setButtonClick(binding.myNotes)
            setUpRecyclerView("my_notes")
            notes_adapter.startListening()
        }

        binding.Friends.setOnClickListener {
            current_button = "Friends"
            setButtonClick(binding.Friends)
            setUpRecyclerView("Friends")
            binding.rv.layoutManager = StaggeredGridLayoutManager(1, LinearLayout.VERTICAL)
            notes_adapter.startListening()
        }


        binding.addNotes.setOnClickListener {
            val intent = Intent(this, edit::class.java)
            intent.putExtra("current_button", current_button)
            startActivity(intent)
        }

        binding.voice.setOnClickListener {
            speechInput()
        }

        binding.searchNotes.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    // Update the adapter with new search options
                    updateAdapter(query)
                }
                return false
            }
        })

        binding.addButton.setOnClickListener {
            showButtonNameDialog()
        }

        restoreButtonState()

        current_button = intent.getStringExtra("currentButton")?: ""

        previous_button_clicked = binding.myNotes

        previous_button_clicked?.setTextColor(Color.WHITE)
        previous_button_clicked?.setBackgroundResource(R.drawable.button_click)

        if (current_button == "") {
            setUpRecyclerView("my_notes")
            current_button = "my_notes"
        } else if (current_button == "All") {
            setUpAllRecyclerView()
        } else {
            setUpRecyclerView(current_button)
        }

        if(intent.getBooleanExtra("notify",false)){
            binding.Friends.performClick()
        }
    }

    private fun setButtonClick(button : Button){
        previous_button_clicked!!.setBackgroundResource(R.drawable.button)
        previous_button_clicked!!.setTextColor(Color.BLACK)
        button.setTextColor(Color.WHITE)
        button.setBackgroundResource(R.drawable.button_click)
        previous_button_clicked = button
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showButtonNameDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_button_dialog, null)
        val buttonNameEditText = dialogView.findViewById<EditText>(R.id.buttonNameEditText)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter Button Name")
            .create()

        okButton.setOnClickListener {
            val buttonName = buttonNameEditText.text.toString()
            if (buttonName.isNotBlank()) {
                addButton(buttonName)
                dialog.dismiss()
            } else {
                // Display an error message or prompt the user to enter a name
                // Handle this according to your app's UI/UX
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addButton(buttonName: String) {
        val newButton = createNewButton(buttonName)
        newButton.setTextColor(Color.BLACK)
        newButton.setOnClickListener {
            Utility.getCollectionReferenceForNotes(buttonName)
            current_button = buttonName
            setButtonClick(newButton)
            setUpRecyclerView(buttonName)
            notes_adapter.startListening()
        }

        newButton.setOnLongClickListener {view->
            showPopupMenuForButton(view, buttonName)
            true
        }

        addNewButtonToLayout(newButton)
        saveButtonState(buttonName)
    }

    private fun createNewButton(buttonName: String): Button {
        val newButton = Button(this)
        newButton.text = buttonName
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            resources.getDimensionPixelSize(R.dimen.dim_10), // 10dp
            resources.getDimensionPixelSize(R.dimen.dim_4), // 4dp
            0, // Right margin (0dp in this case)
            0  // Bottom margin (0dp in this case)
        )
        newButton.layoutParams = layoutParams

        newButton.setPadding(
            resources.getDimensionPixelSize(R.dimen.dim_15),
            0,
            resources.getDimensionPixelSize(R.dimen.dim_15),
            0
        )

        newButton.setBackgroundResource(R.drawable.button)
        newButton.setTextColor(Color.BLACK)

        return newButton
    }

    private fun addNewButtonToLayout(newButton: Button) {
        val layout = findViewById<LinearLayout>(R.id.linearLayout7)
        layout.addView(newButton)
    }

    private fun removeButtonFromLayout(button: Button) {
        val layout = findViewById<LinearLayout>(R.id.linearLayout7)
        layout.removeView(button)
    }

    private fun saveButtonState(newButtonName: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val documentReference = db.collection("Notes")
                .document(currentUser.uid)
                .collection("buttons")
                .document(newButtonName)

            val data = hashMapOf<String, Any>()

            documentReference.set(data)
                .addOnFailureListener { e ->
                    println("Error saving button name to Firestore: ${e.message}")
                }
        }

        val sharedPreferences = getSharedPreferences("Save_buttons_in_array", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("extra_buttons", null)

        val extraButtonsArray = if (json != null) {
            Gson().fromJson(json, Array<String>::class.java).toMutableList()
        } else {
            mutableListOf()
        }

        if (!extraButtonsArray.contains(newButtonName)) {
            extraButtonsArray.add(newButtonName)
            val editor = sharedPreferences.edit()
            editor.putString("extra_buttons", Gson().toJson(extraButtonsArray))
            editor.apply()
        } else {
            println("Button already exists in the array.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun restoreButtonState() {
        val sharedPreferences1 = getSharedPreferences("Save_buttons_in_array", Context.MODE_PRIVATE)
        val json = sharedPreferences1.getString("extra_buttons", null)
        val first = intent.getBooleanExtra("first", false)

        var extraButtonsArray = if (json != null) {
            Gson().fromJson(json, Array<String>::class.java)
        } else {
            arrayOf<String>()
        }

        Log.e("first", first.toString())

        if (first) {
            val currentUser = auth.currentUser
            val buttonsCollection = db.collection("Notes")
                .document(currentUser!!.uid)
                .collection("buttons")

            CoroutineScope(Dispatchers.IO).launch {
                val querySnapshot = buttonsCollection.get().await()
                val buttonNames = querySnapshot.documents.map { it.id }
                val buttonNamesList = buttonNames.toList()
                val reversedButtonNames = buttonNamesList.reversed()
                extraButtonsArray = reversedButtonNames.toTypedArray()

                withContext(Dispatchers.Main) {
                    val sharedPreferences = getSharedPreferences("Save_buttons_in_array", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("extra_buttons", Gson().toJson(extraButtonsArray))
                    editor.apply()

                    // Continue with UI updates
                    updateUIWithButtons(extraButtonsArray)
                }
            }
        } else {
            // No Firebase operation needed, continue with UI updates
            updateUIWithButtons(extraButtonsArray)
        }

        val sharedPreferences = getSharedPreferences("ScrollPosition", MODE_PRIVATE)
        val scrollX = sharedPreferences.getInt("scrollX", 0)
        binding.scrollView2.post {
            Log.d("scrollx retrieve", scrollX.toString())
            binding.scrollView2.scrollTo(scrollX, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateUIWithButtons(extraButtonsArray: Array<String>) {
        for (buttonName in extraButtonsArray.reversedArray()) {
            val newButton = createNewButton(buttonName)
            newButton.setTextColor(Color.BLACK)
            newButton.setOnClickListener {
                current_button = buttonName
                setButtonClick(newButton)
                setUpRecyclerView(buttonName)
                notes_adapter.startListening()
            }
            newButton.setOnLongClickListener { view ->
                showPopupMenuForButton(view, buttonName)
                true
            }
            addNewButtonToLayout(newButton)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPopupMenuForButton(view: View, buttonName: String) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.pop_up_menu, popup.menu)
        popup.setForceShowIcon(true)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    deleteButtonFromFireBase(buttonName)
                    removeButtonFromLayout(view as Button)
                    binding.myNotes.performClick()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun userLoginCheck(){
        // Checking if User is Logged In
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.isEmailVerified
        if (currentUser == null) {
            startActivity(Intent(this@MainActivity, loginPage::class.java))
            finish()
        }
    }

    private fun setUpRecyclerView(name : String) {
        var query: Query?
        if(current_button == "Friends"){
            query = Utility.getCollectionReferenceForNotes(name)
                .orderBy("serial_no", Query.Direction.DESCENDING)
            binding.rv.layoutManager = StaggeredGridLayoutManager(1, LinearLayout.VERTICAL)
        }else {
            query = Utility.getCollectionReferenceForNotes(name)
                .orderBy("serial_no", Query.Direction.DESCENDING)
            binding.rv.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        }

        val options = FirestoreRecyclerOptions.Builder<Note>()
            .setQuery(query, Note::class.java)
            .build()


        notes_adapter = NoteAdapter( this,this, options, current_button)
        binding.rv.adapter = notes_adapter
    }

    private fun setUpAllRecyclerView(){
        val query = Utility.getCollectionReferenceForAll().orderBy("serial_no", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<Note>()
            .setQuery(query, Note::class.java).build()

        current_button = "All"
        binding.rv.layoutManager = StaggeredGridLayoutManager(1, LinearLayout.VERTICAL)

        notes_adapter = NoteAdapter(this, this, options, current_button)
        binding.rv.adapter = notes_adapter
        notes_adapter.startListening()
    }

    override fun onItemClicked(note: Note, docId1: String) {
        val intent = Intent(this@MainActivity, edit::class.java)
        intent.putExtra("docId", docId1)
        intent.putExtra("old_title", note.title?.toString())
        intent.putExtra("old_note", note.content?.toString())
        intent.putExtra("old_date", note.date?.toString())
        intent.putExtra("current_button", current_button)
        intent.putExtra("email",note.email)
        Log.d("current button",current_button)

        startActivity(intent)
        overridePendingTransition(R.anim.pop_out, R.anim.pop_in)
        notes_adapter.startListening()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onLongItemCLicked(note: Note, cardView: CardView, docId: String) {
        this.docId = docId
        //So that no one can delete others post
        if(current_button=="All" && note.email == auth.currentUser?.email.toString()){
            this.popUpDisplay(cardView)
        }
        if(current_button!="All"){
            this.popUpDisplay(cardView)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun popUpDisplay(cardView: CardView) {
        val popup = PopupMenu(this, cardView)

        popup.setOnMenuItemClickListener(this@MainActivity)

        popup.menuInflater.inflate(R.menu.pop_up_menu, popup.menu)
        popup.setForceShowIcon(true)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.delete) {
            deleteNoteFromFireBase(current_button)
            return true
        }
        return false
    }

    private fun deleteButtonFromFireBase(button_name : String) {

        val documentReference = Utility.getCollectionReferenceForNotes("buttons").document(button_name)

        //delete from button list
        documentReference.delete().addOnCompleteListener { task ->
            if (task.isComplete) {
                if (task.isSuccessful) {

                } else {
                    // Note not added to FireStore
                    Toast.makeText(this@MainActivity, "Failed to delete note in FireBase!", Toast.LENGTH_LONG).show()
                }
            }
        }

        //delete the collection
        val documentReference1 = Utility.getCollectionReferenceForNotes(button_name)

        documentReference1.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // Delete each document one by one
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Document deleted successfully
                            Log.d("Delete Document", "Document ${document.id} deleted successfully.")
                        }
                        .addOnFailureListener { e ->
                            // Handle errors here
                            Log.e("Delete Document", "Error deleting document ${document.id}: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle errors here
                Log.e("Delete Documents", "Error retrieving documents: ${e.message}")
            }

        val sharedPreferences = getSharedPreferences("Save_buttons_in_array", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("extra_buttons", null)

        val extraButtonsArray = if (json != null) {
            Gson().fromJson(json, Array<String>::class.java)
        } else {
            arrayOf<String>()
        }

        if (button_name in extraButtonsArray) {
            // Remove the buttonNameToDelete from the array
            val updatedArray = extraButtonsArray.filter { it != button_name }.toTypedArray()

            // Save the updated array back to SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("extra_buttons", Gson().toJson(updatedArray))
            editor.apply()

            // Now, the buttonNameToDelete is removed from the SharedPreferences
        } else {
            // Handle the case where the buttonNameToDelete doesn't exist in the array
            println("Button not found in the array.")
        }

    }

    private fun deleteNoteFromFireBase(button_name : String) {
        val documentReference: DocumentReference
        if(button_name != "All") {
            documentReference =
                Utility.getCollectionReferenceForNotes(button_name).document(docId!!)
        }else{
            documentReference =
                FirebaseFirestore.getInstance().collection("Notes")
                    .document("All").collection("All").document(docId!!)
        }
        documentReference.delete().addOnCompleteListener { task ->
            if (task.isComplete) {
                if (task.isSuccessful) {

                } else {
                    // Note not added to FireStore
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to delete note in FireBase!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Don't use Query.Directions as it will create problem of visibility in search view
    fun updateAdapter(search: String) {
        val query = Utility.getCollectionReferenceForNotes(current_button)
            .orderBy("title") // First orderBy on the title field
            .startAt(search).endAt(search + "\uf8ff") // Apply search filter for title

        val options = FirestoreRecyclerOptions.Builder<Note>()
            .setQuery(query, Note::class.java)
            .build()

        notes_adapter = NoteAdapter(this, this, options,current_button)
        notes_adapter.startListening()
        binding.rv.adapter = notes_adapter
    }

    private fun speechInput() {
        if(!SpeechRecognizer.isRecognitionAvailable(this@MainActivity)){
            Toast.makeText(
                this@MainActivity,
                "Voice to Text Not Supported!",
                Toast.LENGTH_LONG
            ).show()
        }else{
            val intent= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!")
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            startActivityForResult(intent,Speech_Code)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Speech_Code && resultCode== RESULT_OK){
            val extract = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = extract?.get(0).toString()

            binding.searchNotes.setQuery(text, false)
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        val app = applicationContext
        val notificationManager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    private fun showNotificationPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage("Please enable notifications to use this feature.")
        builder.setPositiveButton("Allow") { _: DialogInterface, _: Int ->
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        Log.d("Resume","")
        notes_adapter.notifyDataSetChanged()

        if (homeSelect) {
            binding.bnv.selectedItemId = R.id.home
            homeSelect = false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Start","")
        notes_adapter.startListening() // Start listening when the activity comes into view
        Log.d("onStart", "start")
    }

    override fun onStop() {
        super.onStop()
        Log.d("onStop","")
        val sharedPreferences = getSharedPreferences("ScrollPosition", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("scrollX", 0)
        editor.apply()
        notes_adapter.stopListening()// Stop listening when the activity goes out of view
        Log.d("onStop", "start")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Resume","")
        val scrollX = binding.scrollView2.scrollX
        Log.d("Scrollx save", scrollX.toString())
        val sharedPreferences = getSharedPreferences("ScrollPosition", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("scrollX", scrollX)
        editor.apply()
    }

    fun addCustomDateFormatToAllCollections() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        // You can use a coroutine to make asynchronous Firestore calls
        runBlocking {
            val emailToUserDocRef = db.collection("Notes").document("email_2_user").get().await()

            if (emailToUserDocRef.exists()) {
                val valuesList = emailToUserDocRef.data?.values?.map { it.toString() } ?: emptyList()

                // Use async to make asynchronous Firestore calls and await them
                val results = valuesList.map { uid ->
                    async(Dispatchers.IO) {
                        val userCollections = mutableListOf("my_notes", "Friends")

                        val documentsQuery = db.collection("Notes").document(uid).collection("buttons").get().await()

                        for (document in documentsQuery.documents) {
                            userCollections.add(document.id)
                        }

                        for (collectionName in userCollections) {
                            val collectionRef = db.collection("Notes").document(uid).collection(collectionName)
                            val documents = collectionRef.get().await()

                            val batch = db.batch()
                            for (document in documents) {
                                val existingDateString = document.getString("date")
                                if (existingDateString != null) {
                                    val customFormattedDate = convertToCustomFormat(existingDateString)

                                    // Create a map to update the "serial no" field
                                    val updateData = hashMapOf("serial_no" to customFormattedDate)

                                    val docRef = collectionRef.document(document.id)

                                    // Update the "serial no" field in the document
                                    docRef.update(updateData as Map<String, Any>)
                                        .addOnSuccessListener {
                                            // Update successful
                                            println("Serial no updated for document ${document.id} in collection $collectionName")
                                        }
                                        .addOnFailureListener { e ->
                                            // Handle update failure
                                            println("Error updating serial no for document ${document.id} in collection $collectionName: $e")
                                        }
                                }
                            }


                            try {
                                batch.commit().await()
                                println("Custom date added to documents in collection $collectionName")
                            } catch (e: Exception) {
                                println("Error adding custom date to documents in collection $collectionName: $e")
                            }
                        }
                    }
                }

                // Wait for all async tasks to complete
                results.awaitAll()
            }
        }
    }

    fun addCustomDateFormatToAllCollectionsForCommunity(){
        val db = FirebaseFirestore.getInstance()

        runBlocking {
            val collectionRef = db.collection("Notes").document("All").collection("All")
            val documents = collectionRef.get().await()

            val batch = db.batch()
            for (document in documents) {
                val existingDateString = document.getString("date")
                if (existingDateString != null) {
                    val customFormattedDate = convertToCustomFormat(existingDateString)

                    // Create a map to update the "serial no" field
                    val updateData = hashMapOf("serial_no" to customFormattedDate)

                    val docRef = collectionRef.document(document.id)

                    // Update the "serial no" field in the document
                    docRef.update(updateData as Map<String, Any>)
                        .addOnSuccessListener {
                            // Update successful
                            println("Serial no updated for document ${document.id} in collection ")
                        }
                        .addOnFailureListener { e ->
                            // Handle update failure
                            println("Error updating serial no for document ${document.id} in collection : $e")
                        }
                }
            }


            try {
                batch.commit().await()
                println("Custom date added to documents in collection ")
            } catch (e: Exception) {
                println("Error adding custom date to documents in collection : $e")
            }
        }
    }

    fun convertToCustomFormat(existingDateString: String): String {
        val existingDateFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault())
        val customDateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())

        try {
            val date = existingDateFormat.parse(existingDateString)
            val customDate = customDateFormat.format(date)
            Log.e("Custom Date", customDate)
            return customDate
        } catch (e: ParseException) {
            // Handle the parsing error
            e.printStackTrace()
            return "" // You can return a default value or handle the error as needed.
        }
    }

}