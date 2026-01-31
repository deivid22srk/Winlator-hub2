package com.winlator.panel.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

data class SupabaseRepo(
    val id: Int? = null,
    val name: String,
    val owner: String,
    val repo: String,
    val description: String
)

data class AppConfig(
    val id: Int = 1,
    @SerializedName("dialog_title") val dialogTitle: String,
    @SerializedName("dialog_message") val dialogMessage: String,
    @SerializedName("show_dialog") val showDialog: Boolean
)

interface SupabaseService {
    @GET("rest/v1/repositories?select=*")
    suspend fun getRepositories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<SupabaseRepo>

    @POST("rest/v1/repositories")
    suspend fun createRepository(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Body repo: SupabaseRepo
    )

    @PATCH("rest/v1/repositories")
    suspend fun updateRepository(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") id: String,
        @Body repo: SupabaseRepo
    )

    @DELETE("rest/v1/repositories")
    suspend fun deleteRepository(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") id: String
    )

    @GET("rest/v1/app_config?select=*")
    suspend fun getAppConfig(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<AppConfig>

    @PATCH("rest/v1/app_config")
    suspend fun updateAppConfig(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") id: String,
        @Body config: AppConfig
    )
}

object SupabaseClient {
    const val URL = "https://jbqaegcuitmqfwpsdazn.supabase.co/"
    const val API_KEY = "sb_publishable_TaCuv4LHD-oHAH_jEuqvyQ_BAqV9fbk"
    const val AUTH = "Bearer $API_KEY"
}
