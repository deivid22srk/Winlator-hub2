package com.winlator.downloader.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

data class SupabaseCategory(
    val id: Int? = null,
    val name: String
)

data class SupabaseRepo(
    val id: Int? = null,
    val name: String,
    val owner: String,
    val repo: String,
    val description: String,
    @SerializedName("category_id") val categoryId: Int? = null
)

data class AppConfig(
    @SerializedName("dialog_title") val dialogTitle: String,
    @SerializedName("dialog_message") val dialogMessage: String,
    @SerializedName("show_dialog") val showDialog: Boolean,
    @SerializedName("is_update") val isUpdate: Boolean = false,
    @SerializedName("update_url") val updateUrl: String = "",
    @SerializedName("latest_version") val latestVersion: Int = 1
)

data class SupabaseGameSetting(
    val id: Int? = null,
    val name: String,
    val format: String = "Pré instalado",
    val device: String = "",
    val gamepad: String = "Não",
    @SerializedName("winlator_version") val winlatorVersion: String = "",
    @SerializedName("winlator_repo_owner") val winlatorRepoOwner: String = "",
    @SerializedName("winlator_repo_name") val winlatorRepoName: String = "",
    @SerializedName("winlator_tag_name") val winlatorTagName: String = "",
    @SerializedName("winlator_asset_name") val winlatorAssetName: String = "",
    @SerializedName("winlator_download_url") val winlatorDownloadUrl: String = "",
    @SerializedName("wine_repo_owner") val wineRepoOwner: String = "",
    @SerializedName("wine_repo_name") val wineRepoName: String = "",
    @SerializedName("wine_tag_name") val wineTagName: String = "",
    @SerializedName("wine_asset_name") val wineAssetName: String = "",
    @SerializedName("box64_repo_owner") val box64RepoOwner: String = "",
    @SerializedName("box64_repo_name") val box64RepoName: String = "",
    @SerializedName("box64_tag_name") val box64TagName: String = "",
    @SerializedName("box64_asset_name") val box64AssetName: String = "",
    @SerializedName("gpu_driver_repo_owner") val gpuDriverRepoOwner: String = "",
    @SerializedName("gpu_driver_repo_name") val gpuDriverRepoName: String = "",
    @SerializedName("gpu_driver_tag_name") val gpuDriverTagName: String = "",
    @SerializedName("gpu_driver_asset_name") val gpuDriverAssetName: String = "",
    @SerializedName("dxvk_repo_owner") val dxvkRepoOwner: String = "",
    @SerializedName("dxvk_repo_name") val dxvkRepoName: String = "",
    @SerializedName("dxvk_tag_name") val dxvkTagName: String = "",
    @SerializedName("dxvk_asset_name") val dxvkAssetName: String = "",
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
    @GET("rest/v1/categories?select=*")
    suspend fun getCategories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<SupabaseCategory>

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

    @GET("rest/v1/game_settings?status=eq.approved&select=*&order=id.desc")
    suspend fun getApprovedGameSettings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): List<SupabaseGameSetting>

    @POST("rest/v1/game_settings")
    suspend fun submitGameSetting(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String,
        @Body setting: SupabaseGameSetting
    )
}

object SupabaseClient {
    const val URL = "https://jbqaegcuitmqfwpsdazn.supabase.co/"
    const val API_KEY = "sb_publishable_TaCuv4LHD-oHAH_jEuqvyQ_BAqV9fbk"
    const val AUTH = "Bearer $API_KEY"
}
