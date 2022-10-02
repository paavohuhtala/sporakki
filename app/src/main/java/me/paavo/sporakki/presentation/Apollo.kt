package me.paavo.sporakki.presentation

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache

private const val GRAPHQL_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"

var instance: ApolloClient? = null

fun getApolloClient(context: Context): ApolloClient {
    if (instance != null) {
        return instance!!
    }

    instance = ApolloClient.Builder().serverUrl(GRAPHQL_URL).normalizedCache(
        MemoryCacheFactory(maxSizeBytes = 2 * 1024 * 1024)
    ).build()

    return instance!!
}