package com.company.notemaking.original//package com.company.notemaking
//
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.speech.RecognizerIntent
//import android.speech.SpeechRecognizer
//import android.widget.Toast
//import com.company.notemaking.databinding.ActivityEditBinding
//import com.company.notemaking.models.Note
//import com.company.notemaking.original.MainActivity
//import com.company.notemaking.utilities.Utility
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.DocumentReference
//import java.text.SimpleDateFormat
//import java.util.*
//
//class edit : AppCompatActivity() {
//
//    private lateinit var binding: ActivityEditBinding
//    private lateinit var note: Note
//    private lateinit var old_note: Note
//    var isUpdate = false
//    val speechCode = 105
//    private lateinit var docId: String
//    val auth = FirebaseAuth.getInstance()
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        val i = Intent(this@edit, MainActivity::class.java)
//        startActivity(i)
//        finish()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityEditBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        docId = intent.getStringExtra("docId") ?: ""
//
//        try {
//
//            old_note = intent.getSerializableExtra("current_note") as Note
//            binding.editTitle.setText(old_note.title)
//            binding.editNote.setText(old_note.note)
//            isUpdate=true
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        binding.done.setOnClickListener {
//            val title = binding.editTitle.text.toString()
//            val note_desc = binding.editNote.text.toString()
//
//            if (title.isNotEmpty() || note_desc.isNotEmpty()) {
//                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a")
//
//                note = Note(
//                    title, note_desc, formatter.format(Date())
//                )
//
//                // Save Note to Firebase
////                CoroutineScope(Dispatchers.IO).launch {
//                    saveNoteToFirebase(note, isUpdate)
////                }
//
////                val intent = Intent(this@edit, MainActivity::class.java)
////                intent.putExtra("note", note)
////                setResult(Activity.RESULT_OK, intent)
////                startActivity(intent)
//                finish()
//            } else {
//                Toast.makeText(this@edit, "Please enter some text", Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }
//        }
//
//        binding.back.setOnClickListener {
//            onBackPressed()
//        }
//
//        binding.voice.setOnClickListener {
//            if (!SpeechRecognizer.isRecognitionAvailable(this@edit)) {
//                Toast.makeText(this@edit, "Voice to Speech not available!", Toast.LENGTH_LONG)
//                    .show()
//            } else {
//                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!")
//                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//                i.putExtra(
//                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//                )
//                startActivityForResult(i, speechCode)
//            }
//        }
//    }
//
//    private fun saveNoteToFirebase(note: Note, editMode: Boolean) {
//        val documentReference: DocumentReference
//        if (editMode) {
//
////            Log.d("EditActivity", "Updating note with docId: $docId")
//            documentReference = Utility.getCollectionReferenceForNotes().document(docId)
//
//        } else {
//            documentReference = Utility.getCollectionReferenceForNotes().document()
//        }
//
//
//        documentReference.set(note).addOnCompleteListener { task ->
//            if (task.isComplete) {
//                if (task.isSuccessful) {
//                    // Note added to FireStore
//                    Toast.makeText(this@edit, "Note added Successfully", Toast.LENGTH_LONG).show()
//                } else {
//                    // Note not added to FireStore
//                    Toast.makeText(this@edit, "Failed to add note in FireBase!", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == speechCode && resultCode == RESULT_OK) {
//            val extract = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//            val text = extract?.get(0).toString()
//
//            val note_desc = binding.editNote.text.toString()
//            binding.editNote.setText(note_desc + " " + text)
//        }
//    }
//
//}