package proj.tarotmeter.axl.core.data.config

class StringConfigItem(name: String, defaultValue: String = "") :
  ValueBasedConfigItem<String>(name, defaultValue, { it })
