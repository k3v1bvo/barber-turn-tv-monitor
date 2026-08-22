package com.example.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Singleton provider for the Supabase Kotlin SDK client instance.
 * Provides a thread-safe single instance to interact with database tables, auth, realtime, and storage.
 */
object SupabaseClientProvider {

    private var currentUrl: String = "https://your-supabase-project.supabase.co"
    private var currentKey: String = "your-anon-key"

    private var clientInstance: SupabaseClient? = null

    /**
     * Retrieves or initializes the thread-safe singleton SupabaseClient instance.
     * Re-creates the client if URL or anonKey changes.
     */
    @Synchronized
    fun getInstance(
        supabaseUrl: String = currentUrl,
        supabaseKey: String = currentKey
    ): SupabaseClient {
        val sanitizedUrl = supabaseUrl.trim().removeSuffix("/")
        val sanitizedKey = supabaseKey.trim()

        if (clientInstance == null || currentUrl != sanitizedUrl || currentKey != sanitizedKey) {
            currentUrl = sanitizedUrl
            currentKey = sanitizedKey

            clientInstance = createSupabaseClient(
                supabaseUrl = if (sanitizedUrl.isNotBlank()) sanitizedUrl else "https://your-supabase-project.supabase.co",
                supabaseKey = if (sanitizedKey.isNotBlank()) sanitizedKey else "your-anon-key"
            ) {
                install(Postgrest)
                install(Realtime)
                install(Auth)
                install(Storage)
            }
        }
        return clientInstance!!
    }

    /**
     * Singleton accessor for default client instance.
     */
    val client: SupabaseClient
        get() = getInstance()
}
