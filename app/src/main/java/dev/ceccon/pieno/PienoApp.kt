package dev.ceccon.pieno

import android.app.Application
import dev.ceccon.pieno.data.PienoContainer

class PienoApp : Application() {
    lateinit var container: PienoContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = PienoContainer(this)
    }
}
