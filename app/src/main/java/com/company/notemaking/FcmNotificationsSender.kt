package com.company.notemaking

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class FcmNotificationsSender(
    var userFcmToken: String,
    var title: String,
    var body: String,
    var mContext: Context,
    var mActivity: Activity
) {
    private var requestQueue: RequestQueue? = null
    private val postUrl = "https://fcm.googleapis.com/fcm/send"
    private val fcmServerKey =
        "AAAA5Xt78o8:APA91bGS3dp0zfHsE6TdAibBXlWJCNknrbCqSj5g3HIH-7MLxNKVOOl7INQNE_Em48_vbWbi1L9Yp_-V9F31PhNp3-YXQs7-9KO_4wcoDm9JX1gFdwyCEAuNnceUStxAZWgCMOZJxgjk"

    fun SendNotifications() {
        requestQueue = Volley.newRequestQueue(mActivity)
        val mainObj = JSONObject()
        try {
            mainObj.put("to", userFcmToken)
            val notiObject = JSONObject()
            notiObject.put("title", title)
            notiObject.put("body", body)
            notiObject.put("icon", R.drawable.baseline_notifications_active_24) // enter icon that exists in drawable only
            mainObj.put("notification", notiObject)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, postUrl, mainObj,
                Response.Listener {it ->
                    Log.d("error in fcm", it.toString())
                },
                Response.ErrorListener {it->
                    Log.d("error in fcm", it.printStackTrace().toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=$fcmServerKey"
                    return header
                }
            }
            requestQueue!!.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}