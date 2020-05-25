package com.example.smack.Model

import java.sql.Timestamp

class Message(val message: String, val  userName: String, val channelId: String,
              val userAvatar: String, val userAvatarColor: String, val id: String, val timestamp: String) {
}