package com.example.data.remote

import com.example.BuildConfig
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

    val defaultUrl: String
        get() = BuildConfig.NEXT_PUBLIC_SUPABASE_URL.ifBlank {
            BuildConfig.DEFAULT_SUPABASE_URL.ifBlank { "https://yqzvhtkakmnphoudsadg.supabase.co" }
        }

    val defaultKey: String
        get() = BuildConfig.NEXT_PUBLIC_SUPABASE_ANON_KEY.ifBlank {
            BuildConfig.DEFAULT_SUPABASE_KEY
        }

    private var currentUrl: String = defaultUrl
    private var currentKey: String = defaultKey

    private var clientInstance: SupabaseClient? = null

    /**
     * Retrieves or initializes the thread-safe singleton SupabaseClient instance.
     * Re-creates the client if URL or anonKey changes.
     */
    @Synchronized
    fun getInstance(
        supabaseUrl: String = defaultUrl,
        supabaseKey: String = defaultKey
    ): SupabaseClient {
        val sanitizedUrl = supabaseUrl.trim().removeSuffix("/")
        val sanitizedKey = supabaseKey.trim()

        if (clientInstance == null || currentUrl != sanitizedUrl || currentKey != sanitizedKey) {
            currentUrl = sanitizedUrl
            currentKey = sanitizedKey

            val finalUrl = if (sanitizedUrl.isNotBlank()) sanitizedUrl else defaultUrl
            val finalKey = if (sanitizedKey.isNotBlank()) sanitizedKey else defaultKey

            clientInstance = createSupabaseClient(
                supabaseUrl = finalUrl,
                supabaseKey = finalKey
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

