//package com.company.notemaking.database
//
//import androidx.lifecycle.LiveData
//import androidx.room.*
//import com.company.notemaking.models.Note
//
//@Dao
//interface NoteDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(note : Note)
//
//    @Delete
//    suspend fun delete(note: Note)
//
//    @Update
//    suspend fun update(note: Note)
//
//    @Query("SELECT * FROM notes_table ORDER BY id ASC")
//    fun getAllNotes(): LiveData<List<Note>>
//}