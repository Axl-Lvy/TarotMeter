package proj.tarotmeter.axl.model

enum class Contract(val multiplier: Int, val title: String) {
  Petite(1, "Petite"),
  Garde(2, "Garde"),
  GardeSans(4, "Garde Sans"),
  GardeContre(6, "Garde Contre"),
}
