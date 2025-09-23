package proj.tarotmeter.axl.util

import kotlin.reflect.KClass
import proj.tarotmeter.axl.data.model.Identifiable

/** Simple id generator for classes implementing [Identifiable] */
object IdGenerator {
  private val currentIds: MutableMap<KClass<out Identifiable>, Int> = mutableMapOf()

  /**
   * Returns the next unique id
   *
   * @param clazz the class for which to generate the id
   */
  fun nextId(clazz: KClass<out Identifiable>): Int {
    val nextId = (currentIds[clazz] ?: 0) + 1
    currentIds[clazz] = nextId
    return nextId
  }

  /**
   * Initializes the id generator for a specific class with a starting id. This is useful when
   * loading existing data to ensure new ids do not conflict.
   *
   * @param clazz the class for which to initialize the id generator
   * @param startingId the starting id to set, must be greater than the current id
   */
  fun initialize(clazz: KClass<out Identifiable>, startingId: Int) {
    val currentId = currentIds[clazz] ?: 0
    if (startingId > currentId) {
      currentIds[clazz] = startingId
    }
  }
}
