package proj.tarotmeter.axl.core.data.cloud

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import proj.tarotmeter.axl.util.generated.Secrets

private const val url = "https://hqvnegakqcgltmrzvoxi.supabase.co"

/**
 * Creates the supabase client
 *
 * Note that it is ok to store the key in the source code as it is the anon key.
 */
fun createSupabaseClient(): SupabaseClient {
  return createSupabaseClient(supabaseUrl = url, supabaseKey = Secrets.supabaseApiKey) {
    install(Auth)
    install(Postgrest) { defaultSchema = "tarot_meter" }
  }
}
