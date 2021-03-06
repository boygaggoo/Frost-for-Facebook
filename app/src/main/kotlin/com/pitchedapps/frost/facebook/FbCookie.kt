package com.pitchedapps.frost.facebook

import android.webkit.CookieManager
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.removeCookie
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.SingleSubject

/**
 * Created by Allan Wang on 2017-05-30.
 */
object FbCookie {

    val webCookie: String?
        get() = CookieManager.getInstance().getCookie(FB_URL_BASE)

    fun setWebCookie(cookie: String?, callback: (() -> Unit)?) {
        with(CookieManager.getInstance()) {
            removeAllCookies({
                if (cookie == null) {
                    callback?.invoke()
                    return@removeAllCookies
                }
                L.d("Setting cookie to $cookie")
                val cookies = cookie.split(";").map { Pair(it, SingleSubject.create<Boolean>()) }
                cookies.forEach { (cookie, callback) -> setCookie(FB_URL_BASE, cookie, { callback.onSuccess(it) }) }
                Observable.zip<Boolean, Unit>(cookies.map { (_, callback) -> callback.toObservable() }, {}).subscribeOn(AndroidSchedulers.mainThread()).subscribe({
                    callback?.invoke()
                    L.d("Cookies set: $webCookie")
                    flush()
                })
            })
        }
    }

    operator fun invoke() {
        L.d("User ${Prefs.userId}")
        with(CookieManager.getInstance()) {
            setAcceptCookie(true)
        }
        val dbCookie = loadFbCookie(Prefs.userId)?.cookie
        if (dbCookie != null && webCookie == null) {
            L.d("DbCookie found & WebCookie is null; setting webcookie")
            setWebCookie(dbCookie, null)
        }
    }

    fun save(id: Long) {
        L.d("New cookie found for $id")
        Prefs.userId = id
        CookieManager.getInstance().flush()
        val cookie = CookieModel(Prefs.userId, "", webCookie)
        saveFbCookie(cookie)
    }

    fun reset(callback: () -> Unit) {
        Prefs.userId = Prefs.userIdDefault
        with(CookieManager.getInstance()) {
            removeAllCookies({
                flush()
                callback.invoke()
            })
        }
    }

    fun switchUser(id: Long, callback: () -> Unit) = switchUser(loadFbCookie(id), callback)

    fun switchUser(name: String, callback: () -> Unit) = switchUser(loadFbCookie(name), callback)

    fun switchUser(cookie: CookieModel?, callback: () -> Unit) {
        if (cookie == null) return
        L.d("Switching user to $cookie")
        Prefs.userId = cookie.id
        setWebCookie(cookie.cookie, callback)
    }

    fun logout(id: Long, callback: () -> Unit) {
        L.d("Logging out user $id")
        removeCookie(id)
        reset(callback)
    }
}