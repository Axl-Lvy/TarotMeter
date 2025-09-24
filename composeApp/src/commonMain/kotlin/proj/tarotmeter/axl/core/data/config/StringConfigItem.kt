package proj.tarotmeter.axl.core.data.config

/**
 * A configuration item that stores its value as a [String].
 *
 * @property name The name of the configuration item.
 * @property defaultValue The default value of the configuration item.
 */
class StringConfigItem(name: String, defaultValue: String = "") :
  ValueBasedConfigItem<String>(name, defaultValue, { it })
