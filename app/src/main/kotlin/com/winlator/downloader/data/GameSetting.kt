package com.winlator.downloader.data

data class GameSetting(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val format: String = "Pré instalado",
    val device: String = "",
    val gamepad: String = "Não",
    val winlatorVersion: String = "",
    val winlatorRepoOwner: String = "",
    val winlatorRepoName: String = "",
    val winlatorTagName: String = "",
    val winlatorAssetName: String = "",
    val graphics: String = "",
    val wine: String = "",
    val box64: String = "",
    val box64Preset: String = "",
    val resolution: String = "",
    val gpuDriver: String = "",
    val dxvk: String = "",
    val audioDriver: String = "alsa"
)
