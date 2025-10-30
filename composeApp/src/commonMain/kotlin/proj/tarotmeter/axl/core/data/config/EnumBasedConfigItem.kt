package proj.tarotmeter.axl.core.data.config

/**
 * A configuration item that stores its value as an enum of type [T].
 *
 * The enum value is serialized to its [name][Enum.name] and deserialized using [valueOf].
 *
 * @param T The enum type of the configuration item's value.
 * @property name The name of the configuration item.
 * @property defaultValue The default value of the configuration item.
 * @property valueOf A function that converts a [String] to the enum type [T].
 */
class EnumBasedConfigItem<T : Enum<T>>(
  name: String,
  defaultValue: T,
  private val valueOf: (String) -> T,
) : ValueBasedConfigItem<T>(name, defaultValue, valueOf) {
  companion object {
    inline fun <reified T> from(name: String, default: T): EnumBasedConfigItem<T>
      where T : Enum<T> {
      return EnumBasedConfigItem(name, default) { enumValueOf<T>(it) }
    }
  }
}
