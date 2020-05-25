package com.example.smack.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smack.Model.Channel
import com.example.smack.R
import com.example.smack.Services.AuthService
import com.example.smack.Services.MessageService
import com.example.smack.Services.UserDataService
import com.example.smack.Utilities.BROADCAST_USER_DATA_CHANGED
import com.example.smack.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    private val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // connect to socket server
        socket.connect()
        // when receive "channelCreated" from socket server handle with onNewChannel
        socket.on("channelCreated", onNewChannel)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.syncState()
        setupAdapter()
    }

    override fun onResume() {
        // Receive data from any Activity that send flag "BROADCAST_USER_DATA_CHANGED" and handle with userDataChangeReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(
            BROADCAST_USER_DATA_CHANGED)
        )
        super.onResume()
    }

    override fun onDestroy() {
        // Disconnect wit socket server and not receive data or action from broadcast
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }

    private fun setupAdapter() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter
    }

    private val onNewChannel = Emitter.Listener { args ->
        runOnUiThread {
            val channelName = args[0] as String
            val channelDescription = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName, channelDescription, channelId)
            MessageService.channels.add(newChannel)
            channelAdapter.notifyDataSetChanged()
        }
    }

    // Handle receiving broadcast and manage data
    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (AuthService.isLoggedIn) {
                UserDataService.apply {
                    usernameNavHeader.text = name
                    userEmailNavHeader.text = email
                    val resourceId =
                        resources.getIdentifier(avatarName, "drawable", packageName)
                    userImageNavHeader.setImageResource(resourceId)
                    userImageNavHeader.setBackgroundColor(returnAvatarColor(avatarColor))
                    loginBtnNavHeader.text = getString(R.string.logout)

                    MessageService.getChannels(context) { complete ->
                        if (complete) {
                            channelAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    fun loginBtnNavClicked(view: View) {
        if (AuthService.isLoggedIn) {
            UserDataService.logout()
            loginBtnNavHeader.text = getString(R.string.login)
            usernameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun addChannelClicked(view: View) {
        if (AuthService.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescText)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()

                    // send event "newChannel" with parameter to socket server
                    socket.emit("newChannel", channelName, channelDesc)
                }.setNegativeButton("Cancel") { _, _ ->
                }.show()
        }
    }

    fun sendMessageBtnClicked(view: View) {
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

}
