package proj.tarotmeter.axl.core.data.config

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class ValueBasedConfigItem<T : Any>(
  name: String,
  defaultValue: T,
  private val parser: (String) -> T,
) : KoinComponent, ConfigItem<T>(name, defaultValue) {
  private val settings: Settings by inject()
  private var cachedValue: T? = null

  override var value: T
    get() {
      if (cachedValue == null) {
        val retrievedValue = settings[name, NO_VALUE]
        cachedValue =
          if (retrievedValue == NO_VALUE) {
            defaultValue
          } else {
            parser(retrievedValue)
          }
      }
      val valueToReturn = cachedValue
      checkNotNull(valueToReturn)
      return valueToReturn
    }
    set(value) {
      cachedValue = value
      settings[name] = value.toString()
    }

  override fun reset() {
    settings[name] = defaultValue.toString()
  }
}
