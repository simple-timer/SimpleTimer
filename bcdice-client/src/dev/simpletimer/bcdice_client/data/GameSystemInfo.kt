package dev.simpletimer.bcdice_client.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ゲームシステムの詳細
 *
 * @property id ゲームシステムのID
 * @property name ゲームシステムの名前
 * @property sortKey ゲームシステムをソートするためのキー
 * @property commandPattern 実行可能なコマンドか判定するための正規表現。これにマッチするテキストがコマンドとして実行できる可能性がある。利用する際には大文字か小文字かを無視すること
 * @property helpMessage ヘルプメッセージ
 */
@Serializable
data class GameSystemInfo(
    val id: String,
    val name: String,
    @SerialName("sort_key") val sortKey: String = "",
    @SerialName("command_pattern") val commandPattern: String = "",
    @SerialName("help_message") val helpMessage: String = ""
)
