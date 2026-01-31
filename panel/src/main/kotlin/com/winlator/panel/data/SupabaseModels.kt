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

data class SupabaseGameSetting(
    val id: Int? = null,
    val name: String,
    val format: String = "Pré instalado",
    val device: String = "",
    val gamepad: String = "Não",
    @SerializedName("winlator_version") val winlatorVersion: String = "",
    val graphics: String = "",
    val wine: String = "",
    val box64: String = "",
    @SerializedName("box64_preset") val box64Preset: String = "",
    val resolution: String = "",
    @SerializedName("gpu_driver") val gpuDriver: String = "",
    val dxvk: String = "",
    @SerializedName("audio_driver") val audioDriver: String = "alsa",
    @SerializedName("submitted_by") val submittedBy: String = "",
    @SerializedName("youtube_url") val youtubeUrl: String = "",
    val status: String = "pending"
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

    @GET("rest/v1/game_settings?select=*")
    suspend fun getAllGameSettings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<SupabaseGameSetting>

    @PATCH("rest/v1/game_settings")
    suspend fun updateGameSetting(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") id: String,
        @Body setting: Map<String, String>
    )

    @DELETE("rest/v1/game_settings")
    suspend fun deleteGameSetting(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Query("id") id: String
    )
}

object SupabaseClient {
    const val URL = "https://jbqaegcuitmqfwpsdazn.supabase.co/"
    const val API_KEY = "sb_publishable_TaCuv4LHD-oHAH_jEuqvyQ_BAqV9fbk"
    const val AUTH = "Bearer $API_KEY"
}
