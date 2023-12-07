//package com.company.notemaking.database
//
//import androidx.lifecycle.LiveData
//import com.company.notemaking.models.Note
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.Dispatchers
//
//class noteRepository(private val noteDao: NoteDao) {
//    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
//    val allNotes:LiveData<List<Note>> = noteDao.getAllNotes()
//
//    suspend fun insert(note: Note){
//        noteDao.insert(note)
//    }
//
//    suspend fun delete(note: Note){
//        noteDao.delete(note)
//    }
//
//    suspend fun update(note:Note){
//        noteDao.update(note)
//    }
//
//}