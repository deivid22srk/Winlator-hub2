package com.winlator.downloader.data

data class WinlatorRepo(
    val id: String,
    val name: String,
    val owner: String,
    val repo: String,
    val description: String
)

val WinlatorRepositories = listOf(
    WinlatorRepo("oficial", "Winlator Oficial", "brunodev85", "winlator", "Versão oficial do Winlator"),
    WinlatorRepo("brasil", "Winlator Brasil", "winlatorbrasil", "Winlator-Brasil", "Versão otimizada pela comunidade brasileira"),
    WinlatorRepo("afei", "Winlator Afei", "afeimod", "winlator-mod", "Mod do Winlator por Afei"),
    WinlatorRepo("frost", "Winlator Frost", "MrPhryaNikFrosty", "Winlator-Frost", "Mod do Winlator por Frost"),
    WinlatorRepo("ajay", "Winlator Ajay", "ajay9634", "winlator-ajay", "Mod do Winlator por Ajay"),
    WinlatorRepo("ludashi", "WINLATOR LUDASHI", "Succubussix", "winlator-bionic-glibc", "Versão Bionic Glibc"),
    WinlatorRepo("oss", "WinlatorOSS", "Mart-01-oss", "WinlatorOSS", "Winlator Open Source Software"),
    WinlatorRepo("turnip", "Drivers Turnip", "K11MCH1", "WinlatorTurnipDrivers", "Drivers Turnip para Winlator")
)
