package proj.tarotmeter.axl

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform