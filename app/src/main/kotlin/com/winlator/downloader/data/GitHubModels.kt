package com.winlator.downloader.data

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    val name: String,
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("published_at") val publishedAt: String,
    val body: String,
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String,
    val size: Long
)

interface GitHubService {
    @retrofit2.http.GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @retrofit2.http.Path("owner") owner: String,
        @retrofit2.http.Path("repo") repo: String
    ): List<GitHubRelease>
}
