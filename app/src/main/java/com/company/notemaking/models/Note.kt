package com.company.notemaking.models

class Note {
    var title: String? = null
    var content: String? = null
    var date: String? = null
    var serial_no: String? = null
    var email:String?=null

    constructor()

    constructor(title: String?, content: String?, date: String?, email: String?, serial_no: String?) {
        this.title = title
        this.content = content
        this.date = date
        this.email = email
        this.serial_no = serial_no
    }
}

