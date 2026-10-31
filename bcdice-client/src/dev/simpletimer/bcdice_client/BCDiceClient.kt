package dev.simpletimer.bcdice_client

import dev.simpletimer.bcdice_client.data.GameSystem
import dev.simpletimer.bcdice_client.data.GameSystemInfo
import dev.simpletimer.bcdice_client.data.GameSystemList
import dev.simpletimer.bcdice_client.data.data.RollResult
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.Version
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * BCDice-API v2 のクライアント
 *
 * @param baseUrl APIサーバーのURL
 */
class BCDiceClient(private val baseUrl: String = DEFAULT_BASE_URL) : AutoCloseable {
    companion object {
        const val DEFAULT_BASE_URL = "https://bcdice.simpletimer.dev"

        /**
         * 共有のインスタンス
         */
        val instance: BCDiceClient by lazy { BCDiceClient() }
    }

    //ゲームシステムの一覧のキャッシュ
    @Volatile
    private var gameSystemsCache: List<GameSystem>? = null

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 30_000
        }
    }

    /**
     * リクエストを送り、レスポンスのJSONを文字列で返す
     *
     * @param pathSegments APIのパス
     * @param parameters クエリパラメータ
     * @return ステータスコードとボディ
     */
    private suspend fun request(
        pathSegments: List<String>,
        parameters: Map<String, String> = emptyMap()
    ): Pair<HttpStatusCode, String> {
        val response = client.get(baseUrl) {
            url {
                appendPathSegments(pathSegments)
                parameters.forEach { (key, value) -> this.parameters.append(key, value) }
            }
            accept(ContentType.Application.Json)
        }
        return response.status to response.bodyAsText()
    }

    /**
     * レスポンスのJSONの`ok`を確認する
     */
    private fun isOk(body: String): Boolean {
        return try {
            json.parseToJsonElement(body).jsonObject["ok"]?.jsonPrimitive?.boolean ?: true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * バージョンを取得する
     */
    fun getVersion(): Version = runBlocking {
        val (status, body) = request(listOf("v2", "version"))
        if (!status.isSuccess()) throw BCDiceApiException("Failed to get version: $status")
        json.decodeFromString(body)
    }

    /**
     * ゲームシステムの一覧を取得する
     *
     * @param forceRefresh キャッシュを使わずに再取得するか
     */
    fun getGameSystems(forceRefresh: Boolean = false): List<GameSystem> {
        if (!forceRefresh) gameSystemsCache?.let { return it }
        return runBlocking {
            val (status, body) = request(listOf("v2", "game_system"))
            if (!status.isSuccess()) throw BCDiceApiException("Failed to get game systems: $status")
            json.decodeFromString<GameSystemList>(body).gameSystem.also { gameSystemsCache = it }
        }
    }

    /**
     * ゲームシステムの詳細を取得する
     *
     * @param id ゲームシステムのID
     * @throws UnsupportedGameSystemException ゲームシステムが存在しない時
     */
    fun getGameSystem(id: String): GameSystemInfo = runBlocking {
        val (status, body) = request(listOf("v2", "game_system", id))
        if (status == HttpStatusCode.BadRequest || status == HttpStatusCode.NotFound || !isOk(body)) {
            throw UnsupportedGameSystemException(id)
        }
        if (!status.isSuccess()) throw BCDiceApiException("Failed to get game system $id: $status")
        json.decodeFromString(body)
    }

    /**
     * ダイスロールを行う
     *
     * @param id ゲームシステムのID
     * @param command ダイスのコマンド
     * @return 結果 コマンドが実行できなかった時は[RollResult.ok]がfalseになる
     * @throws UnsupportedGameSystemException ゲームシステムが存在しない時
     */
    fun roll(id: String, command: String): RollResult = runBlocking {
        val (status, body) = request(listOf("v2", "game_system", id, "roll"), mapOf("command" to command))
        if (!isOk(body)) {
            val reason = try {
                json.parseToJsonElement(body).jsonObject["reason"]?.jsonPrimitive?.content
            } catch (_: Exception) {
                null
            }
            if (reason == "unsupported game system") throw UnsupportedGameSystemException(id)
            return@runBlocking RollResult(ok = false)
        }
        if (!status.isSuccess()) throw BCDiceApiException("Failed to roll: $status")
        json.decodeFromString(body)
    }

    override fun close() {
        client.close()
    }
}

/**
 * APIの呼び出しに失敗した時の例外
 */
open class BCDiceApiException(message: String) : RuntimeException(message)

/**
 * 存在しないゲームシステムを指定した時の例外
 */
class UnsupportedGameSystemException(val id: String) : IllegalArgumentException("Unsupported game system: $id")
