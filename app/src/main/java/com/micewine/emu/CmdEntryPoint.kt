package com.micewine.emu

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityThread
import android.app.IActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.IIntentReceiver
import android.content.IIntentSender
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.system.Os
import android.util.Log
import android.view.Surface
import androidx.annotation.Keep
import java.io.DataInputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.exitProcess

@Suppress("DEPRECATION")
@Keep
@SuppressLint("StaticFieldLeak", "UnsafeDynamicallyLoadedCode")
class CmdEntryPoint internal constructor(args: Array<String>?) : ICmdEntryInterface.Stub() {
    init {
        if (!start(args)) exitProcess(1)
        spawnListeningThread()
        sendBroadcastDelayed()
    }

    @SuppressLint("WrongConstant", "PrivateApi")
    fun sendBroadcast() {
        val targetPackage = "com.micewine.emu"
        // We should not care about multiple instances, it should be called only by `Termux:X11` app
        // which is single instance...
        val bundle = Bundle()
        bundle.putBinder("", this)
        val intent = Intent(ACTION_START)
        intent.putExtra("", bundle)
        intent.setPackage(targetPackage)
        if (Os.getuid() == 0 || Os.getuid() == 2000) intent.setFlags(0x00400000 /* FLAG_RECEIVER_FROM_SHELL */)
        try {
            ctx.sendBroadcast(intent)
        } catch (e: IllegalArgumentException) {
            val packageName: String = try {
                ActivityThread.getPackageManager().getPackagesForUid(Os.getuid())[0]
            } catch (ex: RemoteException) {
                throw RuntimeException(ex)
            }
            val am: IActivityManager = try {
                ActivityManager::class.java
                    .getMethod("getService")
                    .invoke(null) as IActivityManager
            } catch (e2: Exception) {
                try {
                    Class.forName("android.app.ActivityManagerNative")
                        .getMethod("getDefault")
                        .invoke(null) as IActivityManager
                } catch (e3: Exception) {
                    throw RuntimeException(e3)
                }
            }
            val sender = am.getIntentSender(
                1, packageName, null, null, 0, arrayOf(intent),
                null, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_ONE_SHOT, null, 0
            )
            try {
                IIntentSender::class.java
                    .getMethod(
                        "send",
                        Int::class.javaPrimitiveType,
                        Intent::class.java,
                        String::class.java,
                        IBinder::class.java,
                        IIntentReceiver::class.java,
                        String::class.java,
                        Bundle::class.java
                    )
                    .invoke(sender, 0, intent, null, null, object : IIntentReceiver.Stub() {
                        override fun performReceive(
                            i: Intent,
                            r: Int,
                            d: String,
                            e: Bundle,
                            o: Boolean,
                            s: Boolean,
                            a: Int
                        ) {
                        }
                    }, null, null)
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }
    }

    // In some cases Android Activity part can not connect opened port.
    // In this case opened port works like a lock file.
    private fun sendBroadcastDelayed() {
        if (!connected()) sendBroadcast()
        handler!!.postDelayed({ sendBroadcastDelayed() }, 1000)
    }

    private fun spawnListeningThread() {
        Thread {
            // New thread is needed to avoid android.os.NetworkOnMainThreadException
            /*
                The purpose of this function is simple. If the application has not been launched
                before running micewine-emu, the initial sendBroadcast had no effect because no one
                received the intent. To allow the application to reconnect freely, we will listen on
                port `PORT` and when receiving a magic phrase, we will send another intent.
             */Log.e("CmdEntryPoint", "Listening port $PORT")
            try {
                ServerSocket(PORT, 0, InetAddress.getByName("127.0.0.1")).use { listeningSocket ->
                    listeningSocket.setReuseAddress(true)
                    while (true) {
                        try {
                            listeningSocket.accept().use { client ->
                                Log.e("CmdEntryPoint", "Somebody connected!")
                                // We should ensure that it is some
                                val b = ByteArray(MAGIC.size)
                                val reader = DataInputStream(client.getInputStream())
                                reader.readFully(b)
                                if (MAGIC.contentEquals(b)) {
                                    Log.e("CmdEntryPoint", "New client connection!")
                                    sendBroadcast()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace(System.err)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace(System.err)
            }
        }.start()
    }

    external override fun windowChanged(surface: Surface)
    external override fun getXConnection(): ParcelFileDescriptor
    external override fun getLogcatOutput(): ParcelFileDescriptor

    companion object {
        const val ACTION_START = "com.micewine.emu.CmdEntryPoint.ACTION_START"
        const val PORT = 7892
        val MAGIC = "0xDEADBEEF".toByteArray()
        private var handler: Handler? = null
        var ctx = createContext()

        init {
            val path = "lib/" + Build.SUPPORTED_ABIS[0] + "/libXlorie.so"
            val loader = CmdEntryPoint::class.java.getClassLoader()
            val res = loader?.getResource(path)
            val libPath = res?.file?.replace("file:", "")?.replace("-v8a", "")?.replace("/base.apk!", "")

            if (libPath != null) {
                try {
                    System.load(libPath)
                } catch (e: Exception) {
                    Log.e("CmdEntryPoint", "Failed to dlopen $libPath", e)
                    System.err.println("Failed to load native library. Did you install the right apk? Try the universal one.")
                    exitProcess(134)
                }
            }

            if (Looper.getMainLooper() == null) {
                Looper.prepareMainLooper()
            }

            handler = Handler()
        }

        /**
         * Command-line entry point.
         *
         * @param args The command-line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            Log.i("CmdEntryPoint", "commit " + BuildConfig.COMMIT)
            handler!!.post { CmdEntryPoint(args) }
            Looper.loop()
        }

        fun requestConnection() {
            System.err.println("Requesting connection...")
            Thread { // New thread is needed to avoid android.os.NetworkOnMainThreadException
                try {
                    Socket("127.0.0.1", PORT).use { socket ->
                        socket.getOutputStream().write(MAGIC)
                    }
                } catch (e: ConnectException) {
                    if (e.message != null && e.message!!.contains("Connection refused")) {
                        Log.e(
                            "CmdEntryPoint",
                            "ECONNREFUSED: Connection has been refused by the server"
                        )
                    } else Log.e(
                        "CmdEntryPoint",
                        "Something went wrong when we requested connection",
                        e
                    )
                } catch (e: Exception) {
                    Log.e("CmdEntryPoint", "Something went wrong when we requested connection", e)
                }
            }.start()
        }

        /**
         * @noinspection DataFlowIssue
         */
        @SuppressLint("DiscouragedPrivateApi")
        fun createContext(): Context {
            return try {
                val f = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe")
                f.isAccessible = true
                val unsafe = f[null]
                (Class.forName("sun.misc.Unsafe").getMethod("allocateInstance", Class::class.java)
                    .invoke(unsafe, ActivityThread::class.java) as ActivityThread)
                    .systemContext
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        external fun start(args: Array<String>?): Boolean
        @JvmStatic
        private external fun connected(): Boolean
    }
}
