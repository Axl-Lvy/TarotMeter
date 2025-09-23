package proj.tarotmeter.axl.data.model

import kotlin.uuid.Uuid

/** Interface for classes with an id */
interface Identifiable {

  /** Unique id */
  val id: Uuid
}
