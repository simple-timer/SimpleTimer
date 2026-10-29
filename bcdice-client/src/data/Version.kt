package dev.simpletimer.bcdice_client

import kotlinx.serialization.Serializable

/**
 * APIとBCDiceのバージョン
 * BCDice-APIのバージョンと、提供するBCDiceのバージョンが返却される
 *
 * @property api BCDice-APIのバージョン
 * @property bcdice 提供するBCDiceのバージョン
 */
@Serializable
data class Version(
    val api: String,
    val bcdice: String
)
