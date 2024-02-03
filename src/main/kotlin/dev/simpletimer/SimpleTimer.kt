package dev.simpletimer

import dev.simpletimer.audio_player.AudioPlayerManager
import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.DataContainer
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.listener.ListenerManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.requests.RestAction
import java.io.IOException
import java.net.ServerSocket
import java.net.URL
import java.util.*

/**
 * すべての始まり
 *
 */
fun main() {
    SimpleTimer()
}


/**
 * メインクラス
 *
 */
class SimpleTimer {
    companion object {
        //バージョン
        const val VERSION = "v2.4.4"

        //多重起動防止のためにロックするポート
        private const val LOCK_PORT = 918

        //インスタンス
        lateinit var instance: SimpleTimer
    }

    //多重起動防止
    private lateinit var socket: ServerSocket

    //データ保存用
    lateinit var dataContainer: DataContainer

    //オーディオプレイヤーのマネージャー
    val audioManager = AudioPlayerManager()

    //起動しているShardのSet
    lateinit var shards: Set<JDA>

    init {
        //returnを使えるようにする
        let {
            //インターネットへの接続を待ちます
            waitInternetConnection()

            //ポートのロックがされていないかを確認します
            if (!checkPortLock()) return@let

            //インスタンスを代入
            instance = this

            //データのクラス
            dataContainer = DataContainer()

            //Tokenを取得
            val token = dataContainer.config.token.value.apply {
                //トークンがないときに終了する
                if (this == "") {
                    //コンソールに出力
                    println("SETUP: Write the token in the \"token\" field of config.yml")
                    return@let
                }
            }

            //RestActionに失敗したとき、デフォルトは何もしないように
            RestAction.setDefaultFailure {}

            startupJDA(token)

            //BCDiceのマネージャーを開始
            BCDiceManager()

            consoleCommand()
        }
    }


    /**
     * ネットワークに接続するまでスレッドを止めます。
     *
     * @author mqrimo
     */
    private fun waitInternetConnection() {
        //インターネットの接続の確認
        return try {
            //Googleに接続する
            val url = URL("https://google.com")
            url.openConnection().getInputStream().use {
                //何もしない
            }
        } catch (ioException: IOException) {
            try {
                //時間をおいて接続を繰り返す
                Thread.sleep(5000)
                waitInternetConnection()
            } catch (interruptedException: InterruptedException) {
                interruptedException.printStackTrace()
            }
        }
    }

    /**
     * ポートのロックの確認を行います。
     *
     * @return 使用するポートがロックされていない場合はtrueを返します
     */
    private fun checkPortLock(): Boolean {
        return try {
            socket = ServerSocket(LOCK_PORT)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * DiscordBotを開始します。
     *
     * @param token DiscordBotのToken
     * @author mqrimo
     */
    private fun startupJDA(token: String) {
        //JDAを作成
        JDABuilder.createDefault(token).apply {
            //リスナーの登録
            addEventListeners(*ListenerManager.listeners)
            //ステータスを設定
            setStatus(OnlineStatus.ONLINE)
            //アクティビティを変更
            setActivity(Activity.customStatus("/helpでヘルプ表示"))

            val shardsCount = dataContainer.config.shardsCount.value.toInt()
            shards = (0 until shardsCount).map {
                //shardを作る
                useSharding(it, shardsCount).build().apply {
                    updateCommands().addCommands(SlashCommandManager.slashCommands).queue()
                }
            }.toSet()
        }
    }

    /**
     * コンソールからの入力に対応をします
     *
     * @author mqrimo
     */
    private fun consoleCommand() {
        //入力を作成
        var input: String?
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            //入力を取得
            input = scanner.nextLine()

            when (input) {
                "exit" -> {
                    //JDAを終了
                    shards.forEach {
                        it.shutdown()
                    }
                    println("Botを終了します...")
                    break
                }

                "convert" -> {
                    //ファイルベースの旧データをデータベースに登録する
                    dataContainer.convertLegacyDataToDatabase()
                }
            }
        }
    }

    /**
     * IDからギルドを取得する
     *
     * @param id ギルドのID
     * @return [Guild]?
     */
    fun getGuild(id: Long): Guild? {
        //すべてのShardを確認
        return shards.map { it.getGuildById(id) }.firstOrNull()
    }

    /**
     * 権限エラーの埋め込みを作成する
     *
     * @param channel [Channel] 該当のチャンネル
     * @return [MessageEmbed]
     */
    fun getErrorEmbed(channel: Channel): MessageEmbed {
        return EmbedBuilder().apply {
            setTitle("SimpleTimer")
            setDescription(VERSION)
            addField("必要な権限が付与されていません", "Botの動作に必要な権限が付与されていません", false)
            addField("該当のチャンネル", "<#${channel.idLong}>", false)
            addField("詳しくはこちらを参照してください", "https://manual.simpletimer.dev/permissions.html", false)
        }.build()
    }
}
