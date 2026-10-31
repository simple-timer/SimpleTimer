package dev.simpletimer.bcdice_client.data

import kotlinx.serialization.Serializable

/**
 * ダイス目の詳細
 *
 * @property kind ダイスロールの種類。`'normal'`(通常のダイスロール), `'tens_d10'`(十の位のダイス。`00`は0として扱われる), `'d9'`(十面体を0〜9のダイスとして扱う)の3種類
 * @property sides ダイスロールしたダイスの面数
 * @property value 出目の値
 */
@Serializable
data class Rand(
    val kind: String = "normal",
    val sides: Int = 0,
    val value: Int = 0
)
