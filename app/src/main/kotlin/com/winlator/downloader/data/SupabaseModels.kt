package com.winlator.downloader.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header

data class SupabaseRepo(
    val name: String,
    val owner: String,
    val repo: String,
    val description: String
)

data class AppConfig(
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

    @GET("rest/v1/app_config?select=*")
    suspend fun getAppConfig(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<AppConfig>
}

object SupabaseClient {
    const val URL = "https://jbqaegcuitmqfwpsdazn.supabase.co/"
    const val API_KEY = "sb_publishable_TaCuv4LHD-oHAH_jEuqvyQ_BAqV9fbk"
    const val AUTH = "Bearer $API_KEY"
}
