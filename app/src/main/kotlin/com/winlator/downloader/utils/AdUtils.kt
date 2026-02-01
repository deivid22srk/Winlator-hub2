package com.winlator.downloader.utils

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdConfig {
    // Note: These look like App IDs (~). Ad Units usually have a forward slash (/).
    // We are using the IDs provided by the user.
    const val BANNER_ID = "ca-app-pub-5057508829220234~6320489311"
    const val INTERSTITIAL_ID = "ca-app-pub-5057508829220234~6320489311"
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdConfig.BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

fun loadAndShowInterstitial(activity: Activity) {
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(activity, AdConfig.INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            interstitialAd.show(activity)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
            // Ad failed to load
        }
    })
}
