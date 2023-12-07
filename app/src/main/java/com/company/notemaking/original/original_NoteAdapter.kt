package com.company.notemaking.original
//
//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.recyclerview.widget.RecyclerView
//import com.company.notemaking.R
//import com.company.notemaking.utilities.Utility
//import com.company.notemaking.models.Note
//import com.firebase.ui.firestore.FirestoreRecyclerAdapter
//import com.firebase.ui.firestore.FirestoreRecyclerOptions
//import com.google.firebase.firestore.Query
//import kotlin.random.Random
//
//class NoteAdapter(private val context: Context, val listener: notesClickListener,
//                  options: FirestoreRecyclerOptions<Note>
//) : FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder>(options) {
//
////    private val notesList = ArrayList<Note>()
////    private val fullList = ArrayList<Note>()
//
////    fun updateList(newList : List<Note>){
////        fullList.clear()
////        fullList.addAll(newList)
////
////        notesList.clear()
////        notesList.addAll(fullList)
////
////        notifyDataSetChanged()
////    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
//        return NoteViewHolder(
//            LayoutInflater.from(parent.context).inflate(R.layout.list, parent, false)
//        )
//    }
//
//    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, model: Note) {
//        Log.d("rv", "adding views")
//        holder.title.text = model.title
//        holder.note.text = model.note
//        holder.date.text = model.date.toString()
//        holder.title.isSelected = true
//        holder.date.isSelected = true
//
//        holder.notes_layout.setCardBackgroundColor(holder.itemView.resources.getColor(randomColor(), null))
//
//        val docId = snapshots.getSnapshot(position).id
//
//        holder.notes_layout.setOnClickListener{
//            listener.onItemClicked(getItem(holder.bindingAdapterPosition), docId)
//        }
//
//        holder.notes_layout.setOnLongClickListener {
//            listener.onLongItemCLicked(getItem(holder.bindingAdapterPosition), holder.notes_layout, docId)
//            true
//        }
//    }
//
//
//
//    class NoteViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
//        val notes_layout = itemView.findViewById<CardView>(R.id.cv_layout)
//        val title = itemView.findViewById<TextView>(R.id.tv_title)
//        val note = itemView.findViewById<TextView>(R.id.tv_note)
//        val date = itemView.findViewById<TextView>(R.id.tv_date)
//    }
//
//    interface notesClickListener{
//        fun onItemClicked(note: Note, docId:String)
//        fun onLongItemCLicked(note: Note, cardView: CardView, docId:String)
//    }
//
//    fun randomColor(): Int{
//        val list = ArrayList<Int>()
//
//        list.add(R.color.noteColor1)
//        list.add(R.color.noteColor2)
//        list.add(R.color.noteColor3)
//        list.add(R.color.noteColor4)
//        list.add(R.color.noteColor5)
//        list.add(R.color.noteColor6)
//
//        val seed = System.currentTimeMillis().toInt()
//        val randomIndex= Random(seed).nextInt(list.size)
//        return list[randomIndex]
//    }
//
////    fun searchList(search : String){
////        notesList.clear()
////
////        for (item in fullList){
////            if (item.title?.lowercase()?.contains(search.lowercase()) == true ||
////                    item.note?.lowercase()?.contains(search.lowercase()) == true){
////                notesList.add(item)
////            }
////        }
////
////        notifyDataSetChanged()
////    }
//
//
//}