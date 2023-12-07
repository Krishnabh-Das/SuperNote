//package com.company.notemaking.models
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.viewModelScope
//import com.company.notemaking.database.noteDatabase
//import com.company.notemaking.database.noteRepository
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class NoteViewModel(application: Application) : AndroidViewModel(application) {
//    val dao = noteDatabase.getDatabase(application).getNoteDao()
//    val repository : noteRepository = noteRepository(dao)
//    val allNotes : LiveData<List<Note>> = repository.allNotes
//
////    fun addNote(note : Note) = viewModelScope.launch ( Dispatchers.IO ){
////        repository.insert(note)
////    }
//
//    suspend fun addNote(note : Note){
//        repository.insert(note)
//    }
//
//    suspend fun deleteNote(note : Note) {
//        repository.delete(note)
//    }
//
//    suspend fun updateNote(note : Note) {
//        repository.update(note)
//    }
//}