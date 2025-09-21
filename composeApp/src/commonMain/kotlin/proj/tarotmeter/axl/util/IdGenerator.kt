package proj.tarotmeter.axl.util

import kotlin.reflect.KClass
import proj.tarotmeter.axl.model.AutoIncrement

/** Simple id generator for classes implementing [AutoIncrement] */
object IdGenerator {
  private val currentIds: MutableMap<KClass<out AutoIncrement>, Int> = mutableMapOf()

  /**
   * Returns the next unique id
   *
   * @param clazz the class for which to generate the id
   */
  fun nextId(clazz: KClass<out AutoIncrement>): Int {
    val nextId = (currentIds[clazz] ?: 0) + 1
    currentIds[clazz] = nextId
    return nextId
  }
}
