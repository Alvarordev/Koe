package com.example.tracker.presentation.subscriptions

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tracker.R

data class SubscriptionIconSpec(
    @param:DrawableRes val iconRes: Int,
    val tint: Color? = null,
    val size: Dp = 24.dp
)

data class KnownSubscription(
    val name: String,
    val iconResName: String,
    val iconSpec: SubscriptionIconSpec
)

object SubscriptionIconCatalog {
    val entries: List<KnownSubscription> = listOf(
        KnownSubscription("Adobe", "sub_adobe", SubscriptionIconSpec(R.drawable.sub_adobe)),
        KnownSubscription("Amazon", "sub_amazon", SubscriptionIconSpec(R.drawable.sub_amazon, tint = Color.White)),
        KnownSubscription("Apple TV", "sub_apple_tv", SubscriptionIconSpec(R.drawable.sub_apple_tv, tint = Color.White)),
        KnownSubscription("Apple Music", "sub_apple_music", SubscriptionIconSpec(R.drawable.sub_apple_music)),
        KnownSubscription("ChatGPT", "sub_chatgpt", SubscriptionIconSpec(R.drawable.sub_chatgpt, tint = Color.White)),
        KnownSubscription("Claude", "sub_claude", SubscriptionIconSpec(R.drawable.sub_claude)),
        KnownSubscription("Crunchyroll", "sub_crunchyroll", SubscriptionIconSpec(R.drawable.sub_crunchyroll)),
        KnownSubscription("Disney+", "sub_disneyplus", SubscriptionIconSpec(R.drawable.sub_disneyplus)),
        KnownSubscription("Figma", "sub_figma", SubscriptionIconSpec(R.drawable.sub_figma)),
        KnownSubscription("HBO Max", "sub_hbo", SubscriptionIconSpec(R.drawable.sub_hbo, tint = Color.White)),
        KnownSubscription("Netflix", "sub_netflix", SubscriptionIconSpec(R.drawable.sub_netflix)),
        KnownSubscription("Spotify", "sub_spotify", SubscriptionIconSpec(R.drawable.sub_spotify)),
        KnownSubscription("Strava", "sub_strava", SubscriptionIconSpec(R.drawable.sub_strava)),
        KnownSubscription("Twitch", "sub_twitch", SubscriptionIconSpec(R.drawable.sub_twitch)),
        KnownSubscription("X (Twitter)", "sub_x", SubscriptionIconSpec(R.drawable.sub_x, tint = Color.White)),
        KnownSubscription("YouTube", "sub_youtube", SubscriptionIconSpec(R.drawable.sub_youtube)),
        KnownSubscription("YouTube Music", "sub_youtube_music", SubscriptionIconSpec(R.drawable.sub_youtube_music)),
    )

    private val map: Map<String, SubscriptionIconSpec> = entries.associate { it.iconResName to it.iconSpec }

    fun forName(name: String): SubscriptionIconSpec? = map[name]
}
