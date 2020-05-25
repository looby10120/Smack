package com.example.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.smack.Controller.App
import com.example.smack.Model.Channel
import com.example.smack.Model.Message
import com.example.smack.Utilities.URL_GET_CHANNEL
import com.example.smack.Utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(context: Context, complete: (Boolean) -> Unit) {

        val channelRequest = object :
            JsonArrayRequest(Method.GET, URL_GET_CHANNEL, null, Response.Listener { response ->
                try {
                    for (i in 0 until response.length()) {
                        val channel = response.getJSONObject(i)
                        val name = channel.getString("name")
                        val channelDesc = channel.getString("description")
                        val channelId = channel.getString("_id")

                        val newChannel = Channel(name, channelDesc, channelId)
                        this.channels.add(newChannel)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                }
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not retrieve channels: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                return HashMap<String, String>().apply {
                    put("Authorization", "Bearer ${App.prefs.authToken}")
                }
            }
        }
        Volley.newRequestQueue(context).add(channelRequest)
    }

    fun getMessages(channelId: String, complete: (Boolean) -> Unit) {

        val url = "${URL_GET_MESSAGES}$channelId"
        val getMsgRequest =
            object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
                clearMessages()
                try {
                    for (i in 0 until response.length()) {
                        val message = response.getJSONObject(i)
                        val msgBody = message.getString("messageBody")
                        val channelId = message.getString("channelId")
                        val userName = message.getString("userName")
                        val userAvatar = message.getString("userAvatar")
                        val userAvatarColor = message.getString("userAvatarColor")
                        val id = message.getString("_id")
                        val timestamp = message.getString("timeStamp")

                        val newMessage = Message(msgBody, userName, channelId, userAvatar, userAvatarColor, id, timestamp)
                        this.messages.add(newMessage)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                }
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not retrieve messages: $error")
                complete(false)
            }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getHeaders(): MutableMap<String, String> {
                    return HashMap<String, String>().apply {
                        put("Authorization", "Bearer ${App.prefs.authToken}")
                    }
                }
            }
        App.prefs.requestQueue.add(getMsgRequest)
    }

    fun clearMessages() {
        messages.clear()
    }

    fun clearChannels() {
        channels.clear()
    }
}