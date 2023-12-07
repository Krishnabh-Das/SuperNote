package com.company.notemaking.adapter

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.company.notemaking.FcmNotificationsSender
import com.company.notemaking.R
import com.company.notemaking.models.Note
import com.company.notemaking.utilities.Utility
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class NoteAdapter (val context : Context,val listener: notesClickListener, options: FirestoreRecyclerOptions<Note>, val currentButton: String)
    : FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder>(options) {

    val sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    var token:String=""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list, parent, false)
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, model: Note) {
        Log.d("rv", "adding views")
        holder.title.text = model.title
        holder.note.text = model.content
        holder.date.text = model.date.toString()
        if(currentButton == "All") {
            holder.email.text=model.email
            holder.email.visibility = View.VISIBLE
            holder.email_logo.visibility=View.VISIBLE
        }
        if(currentButton=="Friends"){
            holder.email.text=model.email
            holder.email.visibility = View.VISIBLE
            holder.email_logo.visibility=View.VISIBLE
        }

        holder.title.isSelected = true
        holder.date.isSelected = true
        setAnimation(holder.notes_layout, position)

        holder.notes_layout.setCardBackgroundColor(holder.itemView.resources.getColor(randomColor(), null))

        val docId = snapshots.getSnapshot(position).id

        // On CLICK Notes
        holder.notes_layout.setOnClickListener{
            listener.onItemClicked(getItem(holder.adapterPosition), docId)
        }

        // On LONG PRESS Note
        holder.notes_layout.setOnLongClickListener {
            listener.onLongItemCLicked(getItem(holder.adapterPosition), holder.notes_layout, docId)
            true
        }

        // SHARE Note
        holder.button.setOnClickListener {
            val note: Note = Note(model.title, model.content, SimpleDateFormat("EEE, d MMM yyyy HH:mm a").format(Date())
                , FirebaseAuth.getInstance().currentUser?.email,SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(Date()) )

            PopUp(holder.notes_layout, note)
        }
    }

    private fun setAnimation(notesLayout: CardView?, position: Int) {
        val slideIn = AnimationUtils.loadAnimation(context, R.anim.rv_animation)
        notesLayout?.startAnimation(slideIn)
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notes_layout = itemView.findViewById<CardView>(R.id.cv_layout)
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val note = itemView.findViewById<TextView>(R.id.tv_note)
        val date = itemView.findViewById<TextView>(R.id.tv_date)
        val button = itemView.findViewById<Button>(R.id.share_button)
        val email = itemView.findViewById<TextView>(R.id.tv_email) // Update to use textView5
        var email_logo = itemView.findViewById<ImageView>(R.id.sender_email) // Update to use sender_email
    }


    interface notesClickListener{
        fun onItemClicked(note: Note, docId:String)
        fun onLongItemCLicked(note: Note, cardView: CardView, docId:String)
    }


    fun randomColor(): Int{
        val list = ArrayList<Int>()

        list.add(R.color.noteColor1)
        list.add(R.color.noteColor3)
        list.add(R.color.noteColor4)
        list.add(R.color.noteColor5)
        list.add(R.color.noteColor6)

        val seed = System.currentTimeMillis().toInt()
        val randomIndex= Random(seed).nextInt(list.size)
        return list[randomIndex]
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun PopUp(notesLayout: CardView?, note: Note) {
        val popup = PopupMenu(context, notesLayout)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.share_All -> {
                    Utility.getCollectionReferenceForAll().document().set(note).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Notes Shared in Community", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to Share Notes", Toast.LENGTH_LONG).show()
                        }
                    }
                    true
                }
                R.id.share_email->{
                    dialog(note)
                    true
                }

                R.id.tabs ->{
                    showingTabs(notesLayout, note)
                    true
                }
                else -> false
            }
        }

        popup.menuInflater.inflate(R.menu.share_menu, popup.menu)
        popup.setForceShowIcon(true)
        popup.show()

    }

    private fun dialog(note: Note) {
        val dialogFrame = LayoutInflater.from(context).inflate(R.layout.share_email_dialog, null)
        val email = dialogFrame.findViewById<AutoCompleteTextView>(R.id.share_email_dialog)
        val sendButton = dialogFrame.findViewById<Button>(R.id.send_email_dialog)
        val cancelButton = dialogFrame.findViewById<Button>(R.id.share_cancelButton)

        val emailSuggestions = getEmailList()

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, emailSuggestions)
        email.setAdapter(adapter)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(dialogFrame)
            .setTitle("Sharing Email")
            .create()

        sendButton.setOnClickListener {
            val email_str = email.text.toString()

            if (email_str.isNotBlank()) {

                // FIREBASE Penetration
                Utility.sendNoteIdByEmail(email_str, context, note){done->
                    if(done){

                        val email_list = getEmailList()
                        email_list.add(email_str)
                        saveEmailList(email_list)

                        FirebaseFirestore.getInstance().collection("Notes").document("email_2_token").get()
                            .addOnSuccessListener { documentSnapshot->
                                if(documentSnapshot.exists()){
                                    Log.d("email string", email_str.lowercase())
                                    val userData = documentSnapshot.data

                                    // Iterate through the fields and find the matching email
                                    for ((key, value) in userData.orEmpty()) {
                                        if (key == email_str) {
                                            token = value.toString()
                                            Log.d("token found", token)
                                            break  // Exit the loop once a match is found
                                        }
                                    }

                                    Log.d("Token value", token)
                                    val notificationsSender = FcmNotificationsSender(token,
                                        "FRIENDS", FirebaseAuth.getInstance().currentUser?.email.toString() + " has shared a Note with You!", context, context as Activity)
                                    notificationsSender.SendNotifications()

                                }else{
                                    Log.d("error","field not found")
                                }
                                }
                            }
                    }
                }

                dialog.dismiss()
            }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun getEmailList(): MutableList<String> {
        val emailSet = sharedPrefs?.getStringSet("emailList", mutableSetOf())
        return emailSet?.toMutableList() ?: mutableListOf()
    }
    fun saveEmailList(emailList: List<String>) {
        val editor = sharedPrefs?.edit()
        editor?.putStringSet("emailList", emailList.toSet())
        editor?.apply()
    }

    fun showingTabs(notesLayout: CardView?, note: Note){
        val user = FirebaseAuth.getInstance().currentUser
        val buttonNames = mutableListOf<String>()

        if(user!=null){
            val buttonRef = FirebaseFirestore.getInstance().collection("Notes").document(user.uid).collection("buttons")
            buttonRef.get().addOnSuccessListener {snapshot->
                for (document in snapshot){
                    buttonNames.add(document.id)
                }

                val popupmenu = PopupMenu(context, notesLayout)
                for (buttonName in buttonNames){
                    popupmenu.menu.add(buttonName)
                }

                popupmenu.setOnMenuItemClickListener { menuItem->
                    val tabName = menuItem.title.toString()
                    FirebaseFirestore.getInstance().collection("Notes").document(user.uid).collection(tabName).document().set(note).addOnSuccessListener {
                        Toast.makeText(context, "Notes Added to "+tabName, Toast.LENGTH_SHORT ).show()
                    }.addOnFailureListener {e ->
                        Toast.makeText(context,e.localizedMessage.toString(),Toast.LENGTH_LONG).show()
                    }
                    true
                }

                popupmenu.show()
            }
        }
    }
}