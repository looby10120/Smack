package com.example.smack.Services

import android.graphics.Color
import java.util.*

object UserDataService {

    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email = ""
    var name = ""

    fun returnAvatarColor(components: String) : Int {
        val strippedColor = components
            .replace("$", "")
            .replace("[", "")
            .replace("]", "")
            .replace(",", "")

        var red = 0
        var green = 0
        var blue = 0

        val scanner = Scanner(strippedColor)
        if (scanner.hasNext()) {
            red = (scanner.nextDouble() * 255).toInt()
            green = (scanner.nextDouble() * 255).toInt()
            blue = (scanner.nextDouble() * 255).toInt()
        }

        return Color.rgb(red, green, blue)
    }

    fun logout() {
        id = ""
        avatarName = ""
        avatarColor = ""
        name = ""
        email = ""
        AuthService.apply {
            authToken = ""
            userEmail = ""
            isLoggedIn = false
        }
    }

}