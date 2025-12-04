package fr.tarotmeter

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Application class for Android to initialize Koin with Android context. */
class TarotMeterApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidContext(this@TarotMeterApplication)
      modules(*initKoinModules())
    }
  }
}
