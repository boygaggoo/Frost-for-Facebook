package com.pitchedapps.frost

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.pitchedapps.frost.dbflow.loadFbCookiesAsync
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchNewTask

/**
 * Created by Allan Wang on 2017-05-28.
 */
class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        L.d("Load cookies ${System.currentTimeMillis()}")
        loadFbCookiesAsync {
            cookies ->
            L.d("Cookies loaded ${System.currentTimeMillis()} $cookies")
            if (cookies.isNotEmpty())
                launchNewTask(if (Prefs.userId != Prefs.userIdDefault) MainActivity::class.java else SelectorActivity::class.java, ArrayList(cookies))
            else
                launchNewTask(LoginActivity::class.java)
        }
    }
}