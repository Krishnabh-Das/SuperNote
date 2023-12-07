package com.company.notemaking.original
//
//import android.content.Intent
//import android.os.Bundle
//import android.speech.RecognizerIntent
//import android.speech.SpeechRecognizer
//import android.util.Log
//import android.view.MenuItem
//import android.widget.LinearLayout
//import android.widget.PopupMenu
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.cardview.widget.CardView
//import androidx.recyclerview.widget.StaggeredGridLayoutManager
//import com.company.notemaking.R
//import com.company.notemaking.adapter.NoteAdapter
//import com.company.notemaking.databinding.ActivityMainBinding
//import com.company.notemaking.edit
//import com.company.notemaking.loginPage
//import com.company.notemaking.models.Note
//import com.company.notemaking.profile
//import com.company.notemaking.utilities.Utility
//import com.firebase.ui.firestore.FirestoreRecyclerOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.Query
//import java.util.*
//
//class originalMainActivity : AppCompatActivity(), NoteAdapter.notesClickListener,
//    PopupMenu.OnMenuItemClickListener {
//
//    private lateinit var binding: ActivityMainBinding
////    private lateinit var database:noteDatabase
////    lateinit var viewModel: NoteViewModel
//    lateinit var notes_adapter: NoteAdapter
//    lateinit var selectedNote: Note
//    private var homeSelect = false
//    val Speech_Code = 101
//    val auth = FirebaseAuth.getInstance()
//    lateinit var docId: String
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Bottom Navigation View
//        val bottomNavigationView = binding.bnv
//
//        // Checking if User is Logged In
//        val auth = FirebaseAuth.getInstance()
//        val currentUser = auth.currentUser?.isEmailVerified
//        if(currentUser == null){
//            startActivity(Intent(this@originalMainActivity, loginPage::class.java))
//            finish()
//        }
//
//        // Initializing the UI
//        setUpRecyclerView()
//        initUI()
//        Log.d("onCreate", "start")
//
//
//        // Checking if user if coming from login Activity    ->     if yes than retrieve data from fireStore
////        var fromLogin = intent.getBooleanExtra("fromLogin", false)
////        if (fromLogin) {
////            getFireStoreList { notesList ->
////                for (note in notesList) {
////                    if (note != null) {
////                        viewModel.viewModelScope.launch {
////                            Log.d("viewModel addNote", "added")
////                            viewModel.addNote(note)
////                        }
////                    }
////                }
////            }
////            fromLogin = false
////        }
//
//
//        // Navigation Bar Items
//        bottomNavigationView.setOnItemSelectedListener { menuItem ->
//            when(menuItem.itemId){
//                R.id.home ->{
//                    if(this@originalMainActivity !is originalMainActivity){
//                        val i = Intent(this@originalMainActivity, originalMainActivity::class.java)
//                        startActivity(i)
//                        finish()
//                    }
//                    true
//                }
//                R.id.profile ->{
//                    if(auth.currentUser?.isEmailVerified == false || auth.currentUser == null) {
//                        val intent = Intent(this@originalMainActivity, loginPage::class.java)
//                        startActivity(intent)
//                        homeSelect = true
//                    }else{
//                        val intent = Intent(this@originalMainActivity, profile::class.java)
//                        startActivity(intent)
//                        homeSelect=true
//                    }
//                    true
//                }
//                else -> {
//                    false
//                }
//            }
//        }
//
////        viewModel = ViewModelProvider(this,
////            ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NoteViewModel::class.java)
////
////        viewModel.allNotes.observe(this) {list ->
////            list?.let {
////                adapter.updateList(list)
////            }
////        }
//
////        database = noteDatabase.getDatabase(this)
//    }
//
////    private val updateNote = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
////        if(result.resultCode==Activity.RESULT_OK){
////            val note = result.data?.getSerializableExtra("note") as? Note
////            if(note!=null){
////                viewModel.viewModelScope.launch {
////                    viewModel.updateNote(note)
////                }
////            }
////        }
////    }
//
//
//
//    private fun initUI() {
//
////        val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
////            if(result.resultCode==Activity.RESULT_OK){
////
////                val note = result.data?.getSerializableExtra("note") as? Note
////                if(note !=null){
////                    viewModel.viewModelScope.launch {
////                        viewModel.addNote(note)
////                    }
////                }
////
////            }
////        }
//
//        binding.addNotes.setOnClickListener{
//
//            val intent = Intent(this, edit::class.java)
//            startActivity(intent)
////            getContent.launch(intent)
//        }
//
////        binding.searchNotes.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
////            override fun onQueryTextSubmit(p0: String?): Boolean {
////                return false;
////            }
////
////            override fun onQueryTextChange(newText: String?): Boolean {
////                if(newText!=null){
////                    Log.d("Option", "new option setting")
////                    updateAdapter(getSearchOptions(newText))
////                }
////                return true
////            }
////
////        })
//
////        binding.searchNotes.setOnCloseListener {
////            val options = getOptionFireStore()
////            Log.d("DataRetrieval", "Options size: ${options.snapshots.size}")
////            adapter = NoteAdapter(this,this, options)
////            binding.rv.adapter=adapter
////            false // Return false to allow the SearchView to close
////        }
//
//        binding.voice.setOnClickListener {
//            speechInput()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        notes_adapter.startListening()
//        Log.d("onResume", "start")
//        if(homeSelect){
//            binding.bnv.selectedItemId = R.id.home
//            homeSelect = false
//        }
//    }
//
//    // Callback function waits for the notesList to collect all the data after that it returns the list
////    fun getFireStoreList(callback: (ArrayList<Note>) -> Unit) {
////        val notesList = ArrayList<Note>()
////
////        val currentUser = auth.currentUser
////
////        val notesCollection = FirebaseFirestore.getInstance()
////            .collection("Notes")
////            .document(currentUser?.uid.toString())
////            .collection("my_notes")
////
////        notesCollection.get()
////            .addOnSuccessListener { querySnapshot ->
////                for (documentSnapshot in querySnapshot.documents) {
////                    val note = documentSnapshot.toObject(Note::class.java)
////                    if (note != null) {
////                        notesList.add(note)
////                    }
////                }
////                Log.d("getting fire store data", "get fire store data")
////                callback(notesList)
////            }
////            .addOnFailureListener { exception ->
////                // Handle the failure here
////                callback(notesList) // You might want to handle this differently
////            }
////    }
//
//    override fun onItemClicked(note: Note, docId : String){
//            val intent = Intent(this@originalMainActivity, edit::class.java)
//            intent.putExtra("docId", docId)
//            intent.putExtra("current_note", note)
//            startActivity(intent)
////            updateNote.launch(intent)
//    }
//
//    override fun onLongItemCLicked(note: Note, cardView: CardView, docId : String) {
//        selectedNote = note
//        this.docId =docId
//        popUpDisplay(cardView)
//    }
//
//    private fun popUpDisplay(cardView: CardView) {
//        val popup = PopupMenu(this, cardView)
//
//        popup.setOnMenuItemClickListener(this@originalMainActivity)
//
//        popup.menuInflater.inflate(R.menu.pop_up_menu, popup.menu)
//        popup.show()
//    }
//
//    override fun onMenuItemClick(item: MenuItem?): Boolean {
//        if (item?.itemId == R.id.delete) {
////            viewModel.viewModelScope.launch {
////                viewModel.deleteNote(selectedNote)
////            }
//            deleteNoteFromFireBase()
//            return true
//        }
//        return false
//    }
//
//    private fun deleteNoteFromFireBase() {
//
//        var documentReference = Utility.getCollectionReferenceForNotes().document(docId)
//
//        documentReference.delete().addOnCompleteListener { task ->
//            if (task.isComplete) {
//                if (task.isSuccessful) {
//
//                } else {
//                    // Note not added to FireStore
//                    Toast.makeText(
//                        this@originalMainActivity,
//                        "Failed to delete note in FireBase!",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }
//
//
//    fun setUpRecyclerView(){
//        var query = Utility.getCollectionReferenceForNotes()
//            .orderBy("date", Query.Direction.ASCENDING)
//        query.get().addOnSuccessListener { querySnapshot ->
//            for (documentSnapshot in querySnapshot.documents) {
//                val note = documentSnapshot.toObject(Note::class.java)
//                Log.d(
//                    "FirestoreData",
//                    "Title: ${note?.title}, Note: ${note?.note}, Date: ${note?.date}"
//                )
//            }
//        }
//        var options = FirestoreRecyclerOptions.Builder<Note>()
//            .setQuery(query, Note::class.java)
//            .build()
//
//        binding.rv.setHasFixedSize(true)
//        binding.rv.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
//        Log.d("DataRetrieval", "Options size: ${options.snapshots.size}")
//        notes_adapter = NoteAdapter(this, this, options)
//        binding.rv.adapter=notes_adapter
//    }
//
//    private fun speechInput() {
//        if(!SpeechRecognizer.isRecognitionAvailable(this@originalMainActivity)){
//            Toast.makeText(
//                this@originalMainActivity,
//                "Voice to Text Not Supported!",
//                Toast.LENGTH_LONG
//            ).show()
//        }else{
//            val intent= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!")
//            intent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            startActivityForResult(intent,Speech_Code)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode==Speech_Code && resultCode== RESULT_OK){
//            val extract = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//            val text = extract?.get(0).toString()
//
////            binding.searchNotes.setQuery(text, false)
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        notes_adapter.startListening() // Start listening when the activity comes into view
//        Log.d("onStart", "start")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        notes_adapter.stopListening() // Stop listening when the activity goes out of view
//        Log.d("onStop", "start")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Log.d("onPause", "start")
//    }
//
//    override fun onRestart() {
//        super.onRestart()
//        Log.d("onRestart", "start")
//    }
//
//
////    private fun updateAdapter(newOptions: FirestoreRecyclerOptions<Note>) {
////        notes_adapter.updateOptions(newOptions)
//////        binding.rv.adapter = adapter
////    }
//
//
////    fun getSearchOptions(search: String): FirestoreRecyclerOptions<Note>{
////        val query = Utility.getCollectionReferenceForNotes()
////            .orderBy("date", Query.Direction.ASCENDING)
////        query.whereEqualTo("title", search)
////
////        val options = FirestoreRecyclerOptions.Builder<Note>()
////            .setQuery(query, Note::class.java)
////            .build()
////        return options
////    }
//}