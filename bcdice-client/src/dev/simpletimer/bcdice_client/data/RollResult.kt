package dev.simpletimer.bcdice_client.data.data

import dev.simpletimer.bcdice_client.Rand
import kotlinx.serialization.Serializable

/**
 * ダイスロールの結果
 *
 * @property ok リクエストが成功したか
 * @property text コマンドの出力
 * @property secret シークレットダイスか
 * @property success 結果が成功か
 * @property failure 結果が失敗か
 * @property critical 結果がクリティカルか
 * @property fumble 結果がファンブルか
 * @property rands ダイス目の詳細
 */
@Serializable
data class RollResult(
    val ok: Boolean = false,
    val text: String = "",
    val secret: Boolean = false,
    val success: Boolean = false,
    val failure: Boolean = false,
    val critical: Boolean = false,
    val fumble: Boolean = false,
    val rands: List<Rand> = emptyList()
)
