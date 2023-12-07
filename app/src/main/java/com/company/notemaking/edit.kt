package com.company.notemaking


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.company.notemaking.databinding.ActivityEditBinding
import com.company.notemaking.models.Note
import com.company.notemaking.utilities.Utility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class edit : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityEditBinding
    var isUpdate = false
    private var title: String? = ""
    private var content: String? = ""
    private var docId: String? = null
    private var date: String? = ""
    private var current_button : String = ""
    val speechCode = 102
    private lateinit var alertdialog : AlertDialog
    private lateinit var link_str:String
    private var previous_state = Stack<String>()
    private  var isBack = false
    val fullURL ="https://f501-34-28-28-90.ngrok-free.app/summarize"
    private val auth = FirebaseAuth.getInstance()
    var email: String? =null


    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar3.visibility = View.INVISIBLE
        binding.timeTaken.visibility = View.INVISIBLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) // Add this flag
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }

        // Receive data
        title = intent.getStringExtra("old_title")
        content = intent.getStringExtra("old_note")
        date = intent.getStringExtra("old_date")
        docId = intent.getStringExtra("docId")
        current_button = intent.getStringExtra("current_button").toString()
        email = intent.getStringExtra("email").toString()

        if (!docId.isNullOrEmpty()) {
            isUpdate = true
            previous_state.push(content)
        }else{
            previous_state.push("")
        }

        // Setting the Note
        binding.editTitle.setText(title)
        binding.editNote.setText(content)

        binding.voice.setOnClickListener {
            if (!SpeechRecognizer.isRecognitionAvailable(this@edit)) {
                Toast.makeText(this@edit, "Voice to Speech not available!", Toast.LENGTH_LONG)
                    .show()
            } else {
                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!")
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                i.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                startActivityForResult(i, speechCode)
            }
        }

        binding.done.setOnClickListener {
            if (current_button == "All" && email == FirebaseAuth.getInstance().currentUser?.email.toString()) {
                saveNoteToFirebase(isUpdate) // Save note before navigation
            } else if (current_button != "All") {
                saveNoteToFirebase(isUpdate)
            }

            val i = Intent(this@edit, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // Use these flags
            i.putExtra("currentButton", current_button)
            startActivity(i)

            // Finish the current edit activity (if needed)
            finish()
        }


        binding.back.setOnClickListener {
            if (previous_state.isEmpty()) {
                onBackPressed()
                return@setOnClickListener
            }

            if (previous_state.size == 1) {
                val r = previous_state.pop()
                if (!previous_state.isEmpty()) {
                    binding.editNote.setText(previous_state.peek())
                }else{
                    onBackPressed()
                }
            } else {
                val t = previous_state.pop()
                val r = previous_state.pop()
                if (!previous_state.isEmpty()) {
                    binding.editNote.setText(previous_state.peek())
                }
            }
        }


        binding.editNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!isBack){
                    Log.d("text_changed", s.toString())
                    if(s.toString() != content && s.toString()!=null && s.toString()!="") {
                        previous_state.push(s.toString())
                    }
                }else{
                    isBack = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editNote.setOnLongClickListener {
            popUpMenu()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun popUpMenu() {
        val popup = PopupMenu(this@edit, binding.editNote)
        popup.setOnMenuItemClickListener(this@edit)
        popup.menuInflater.inflate(R.menu.tools, popup.menu)
        popup.setForceShowIcon(true)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.summarize_tool){
            if(binding.editNote.text.isNotEmpty()) {
                timer()
                summarize("", "summary")
                return true
            }else{
                binding.editNote.error = "Empty Note!!"
            }
        }
        if(item?.itemId == R.id.yt_tool){
            dialogShow()
            return true
        }

        return false
    }

    private fun dialogShow() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Link")

        val view = layoutInflater.inflate(R.layout.yt_dialog, null)
        val link = view.findViewById<EditText>(R.id.yt_link) // Find within the inflated view
        val button = view.findViewById<Button>(R.id.getNotes) // Find within the inflated view
        button.setOnClickListener {
            link_str = link.text.toString()
            alertdialog.dismiss()
            summarize(link_str, "NotSummary")
        }

        builder.setView(view)
        alertdialog = builder.create()
        alertdialog.show()
    }

    private fun saveNoteToFirebase(editMode: Boolean) {

        val title = binding.editTitle.text.toString()
        val note_desc = binding.editNote.text.toString()

        val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")

        val documentReference:DocumentReference

        // Saving the Note in Database
        if(current_button == "All"){
            documentReference = if (editMode) {
                FirebaseFirestore.getInstance().collection("Notes") .document("All").collection("All")
                    .document(docId!!)
            } else {
                FirebaseFirestore.getInstance().collection("Notes") .document("All").collection("All")
                    .document()
            }
        }
        else{
            documentReference = if (editMode) {
                Log.d("current in edit", current_button)
                Utility.getCollectionReferenceForNotes(current_button).document(docId!!)
            } else {
                Utility.getCollectionReferenceForNotes(current_button).document()
            }
        }

        if(current_button!="All" && current_button!="Friends"){
            val note = Note(title, note_desc, formatter.format(Date()),"", SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(Date()))

            documentReference.set(note).addOnCompleteListener { task ->
                if (task.isSuccessful) {Log.d("Added","Note") }
                else { Toast.makeText(this@edit, "Failed to add note in FireBase!", Toast.LENGTH_LONG).show() }
            }
        }else{
            val shareNote = if(editMode) {
                Note(title, note_desc, formatter.format(Date()), email, SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(Date()))
            }else{
                Note(title, note_desc, formatter.format(Date()), auth.currentUser?.email.toString(), SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(Date()))
            }
            documentReference.set(shareNote).addOnCompleteListener { task ->
                if (task.isSuccessful) { Log.d("Added","Note")}
                else { Toast.makeText(this@edit, "Failed to add note in FireBase!", Toast.LENGTH_LONG).show() }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == speechCode && resultCode == RESULT_OK) {
            val extract = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = extract?.get(0).toString()

            val note_desc = binding.editNote.text.toString()
            binding.editNote.setText(note_desc + " " + text)
        }
    }

    private fun timer() {
        var startTime = System.currentTimeMillis()
        val job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis() - startTime
                val seconds = (currentTime / 1000).toInt()
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60

                val timeText = String.format("%02d:%02d", minutes, remainingSeconds)
                binding.timeTaken.text = "Time Taken: $timeText"

                delay(1000)
            }
        }
    }

    private fun summarize(link:String, type:String) {

        val request: Request
        timer()

        binding.progressBar3.visibility = View.VISIBLE
        binding.timeTaken.visibility = View.VISIBLE

        val client = OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(6000, TimeUnit.SECONDS)
            .writeTimeout(6000, TimeUnit.SECONDS).build()


        val formBody: RequestBody = if (type == "summary") {
            FormBody.Builder()
                .add("type", type)
                .add("text", binding.editNote.text.toString())
                .add("value", "1")
                .build()
        } else {
            FormBody.Builder()
                .add("type", type)
                .add("link", link)
                .add("value", "1")
                .build()
        }

        request = Request.Builder()
            .url(fullURL)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                // Read data on the worker thread
                val response_data = response.body!!.string()

                // Run view-related code back on the main thread.
                // Here we display the response message in our text view
                runOnUiThread {
                    previous_state.push(response_data)
                    binding.editNote.setText(response_data)
                    binding.progressBar3.visibility = View.INVISIBLE
                    binding.timeTaken.visibility = View.INVISIBLE
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        binding.done.performClick()
    }
}