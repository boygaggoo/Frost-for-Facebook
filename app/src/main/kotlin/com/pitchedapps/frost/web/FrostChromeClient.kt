package com.pitchedapps.frost.web

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.pitchedapps.frost.utils.L
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostChromeClient(val progressObservable: Subject<Int>, val titleObservable: BehaviorSubject<String>) : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        L.v("Chrome Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        if (title.contains("http") || titleObservable.value == title) return
        titleObservable.onNext(title)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressObservable.onNext(newProgress)
    }

}