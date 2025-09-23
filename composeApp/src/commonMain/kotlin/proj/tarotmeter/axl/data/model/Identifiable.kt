package proj.tarotmeter.axl.data.model

import kotlin.uuid.Uuid

/** Interface for classes with auto-incremented id */
interface Identifiable {

  /** Unique id */
  val id: Uuid
}
