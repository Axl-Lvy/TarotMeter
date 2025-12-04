package fr.axllvy.tarotmeter.core.data.config

abstract class ConfigItem<T : Any>(protected val name: String, protected val defaultValue: T) {

  abstract var value: T

  abstract fun reset()

  companion object {
    protected const val NO_VALUE = "NO_VALUE"
  }
}
