package com.example.smack.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.smack.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    var isLoggedIn = false
    var userEmail = ""
    var authToken = ""

    fun registerUser(
        context: Context,
        email: String,
        password: String,
        complete: (Boolean) -> Unit
    ) {

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val requestBody = jsonBody.toString()

        // Expecting String
        val registerRequest =
            object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
                println(response)
                complete(true)
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not register user: $error")
                complete(false)
            }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray()
                }
            }
        Volley.newRequestQueue(context).add(registerRequest)
    }

    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val requestBody = jsonBody.toString()

        // Expecting JsonObject
        val loginRequest =
            object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->
                // this is where we parse the json object
                try {
                    userEmail = response.getString("user")
                    authToken = response.getString("token")
                    isLoggedIn = true
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener { error ->
                // this is where we deal with our error
                Log.d("ERROR", "Could not login user: $error")
                complete(false)
            }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray()
                }
            }
        Volley.newRequestQueue(context).add(loginRequest)
    }

    fun createUser(
        context: Context,
        name: String,
        email: String,
        avatarName: String,
        avatarColor: String,
        complete: (Boolean) -> Unit
    ) {

        val jsonBody = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("avatarName", avatarName)
            put("avatarColor", avatarColor)
        }
        val requestBody = jsonBody.toString()

        val createRequest =
            object : JsonObjectRequest(
                Method.POST,
                URL_CREATE_USER,
                null,
                Response.Listener { response ->
                    try {
                        response.apply {
                            UserDataService.name = getString("name")
                            UserDataService.email = getString("email")
                            UserDataService.avatarName = getString("avatarName")
                            UserDataService.avatarColor = getString("avatarColor")
                            UserDataService.id = getString("_id")
                        }
                        complete(true)
                    } catch (e: JSONException) {
                        Log.d("JSON", "EXC: " + e.localizedMessage)
                        complete(false)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not login user: $error")
                    complete(false)
                }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray()
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $authToken"
                    return headers
                }
            }
        Volley.newRequestQueue(context).add(createRequest)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
        val findUserRequest = object : JsonObjectRequest(
            Method.GET,
            "$URL_GET_USER$userEmail",
            null,
            Response.Listener { response ->
                try {
                    response.apply {
                        UserDataService.name = getString("name")
                        UserDataService.email = getString("email")
                        UserDataService.avatarName = getString("avatarName")
                        UserDataService.avatarColor = getString("avatarColor")
                        UserDataService.id = getString("_id")
                    }

                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGED)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not find user: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $authToken"
                return headers
            }
        }
        Volley.newRequestQueue(context).add(findUserRequest)
    }
}
