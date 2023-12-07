//package com.company.notemaking.database
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.company.notemaking.models.Note
//import com.company.notemaking.utilities.DATABASE_NAME
//
//@Database(entities = arrayOf(Note::class), version=1, exportSchema = false)
//abstract class noteDatabase : RoomDatabase() {
//    abstract fun getNoteDao(): NoteDao
//
//    companion object{
//
//        @Volatile
//        private var INSTANCE : noteDatabase?=null
//
//        fun getDatabase(context : Context): noteDatabase{
//            return INSTANCE ?: synchronized(this){
//
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    noteDatabase::class.java,
//                    DATABASE_NAME
//                ).build()
//
//                INSTANCE=instance
//
//                instance
//            }
//        }
//    }
//
//}