package proj.tarotmeter.axl.core.data.config

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A configuration item that stores its value in a [Settings] instance, using a parser function to
 * convert from [String] to the desired type [T].
 *
 * @param T The type of the configuration item's value.
 * @property name The name of the configuration item.
 * @property defaultValue The default value of the configuration item.
 * @property parser A function that converts a [String] to the desired type [T].
 */
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
    cachedValue = defaultValue
    settings[name] = defaultValue.toString()
  }
}
