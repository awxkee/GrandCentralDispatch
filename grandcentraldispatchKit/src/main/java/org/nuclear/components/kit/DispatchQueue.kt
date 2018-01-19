package org.nuclear.components.kit

import android.os.Looper
import android.os.Handler
import android.os.Message
import java.util.concurrent.CountDownLatch

/**
 * Created by rodionbartoshyk on 05/01/2018.
 */

val mainThread = DispatchQueue.main

val uiThread = mainThread

fun doAsync(runnable: () -> Unit) = DispatchQueue.global.post(runnable)
fun uiThread(runnable: () -> Unit) = uiThread.post(runnable)

public class DispatchQueue(threadName: String) : Thread() {

    companion object {
        val global by lazy {
            DispatchQueue("Global Async Thread")
        }
        val main by lazy {
            Handler(Looper.getMainLooper())
        }
    }

    private val handler: Handler by lazy {
        Handler()
    }
    private val syncLatch = CountDownLatch(1)

    init {
        name = threadName
        start()
    }

    private fun sendMessage(msg: Message, delay: Long) {
        try {
            syncLatch.await()
            if (delay <= 0) {
                handler.sendMessage(msg)
            } else {
                handler.sendMessageDelayed(msg, delay)
            }
        } catch (ignored: Exception) {
        }

    }

    fun cancelRunnable(runnable: Runnable) {
        try {
            syncLatch.await()
            handler.removeCallbacks(runnable)
        } catch (ignored: Exception) {

        }
    }

    fun cancelRunnable(runnable: () -> Unit) {
        try {
            syncLatch.await()
            handler.removeCallbacks(runnable)
        } catch (ignored: Exception) {

        }
    }

    fun execute(runnable: () -> Unit): Any = post(runnable)

    fun post(runnable: () -> Unit): Any = postRunnable(runnable, 0)

    @JvmOverloads
    fun post(runnable: () -> Unit, delay: Long = 0) = postRunnable(runnable, delay)

    @JvmOverloads
    fun postRunnable(runnable: () -> Unit, delay: Long = 0)
    {
        try {
            syncLatch.await()
            if (delay <=0 ) handler.post(runnable)
            else handler.postDelayed(runnable, delay)
        }
        catch (ignored: Exception){
        }
    }

    @JvmOverloads
    fun postRunnable(runnable: Runnable, delay: Long = 0) {
        try {
            syncLatch.await()
            if (delay <= 0) {
                handler.post(runnable)
            } else {
                handler.postDelayed(runnable, delay)
            }
        } catch (ignored: Exception) {
        }
    }

    fun cleanupQueue() {
        try {
            syncLatch.await()
            handler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
        }

    }

    override fun run() {
        Looper.prepare()
        handler
        syncLatch.countDown()
        Looper.loop()
    }

}