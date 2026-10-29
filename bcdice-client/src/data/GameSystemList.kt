package dev.simpletimer.bcdice_client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ゲームシステムの一覧のレスポンス
 * BCDice-APIで利用できるゲームシステムの一覧で、`sort_key`でソートされた状態で返却される
 *
 * @property gameSystem ゲームシステムの一覧
 */
@Serializable
internal data class GameSystemList(
    @SerialName("game_system") val gameSystem: List<GameSystem>
)
