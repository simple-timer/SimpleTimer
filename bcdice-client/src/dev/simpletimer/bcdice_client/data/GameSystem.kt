package dev.simpletimer.bcdice_client.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ゲームシステムの一覧の要素
 *
 * @property id ゲームシステムのID
 * @property name ゲームシステムの名前
 * @property sortKey ゲームシステムをソートするためのキー
 */
@Serializable
data class GameSystem(
    val id: String,
    val name: String,
    @SerialName("sort_key") val sortKey: String = ""
)
