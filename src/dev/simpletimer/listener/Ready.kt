package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import dev.simpletimer.database.transaction.TimerDataTransaction
import dev.simpletimer.database.transaction.TimerQueueTransaction
import dev.simpletimer.timer.Timer
import dev.simpletimer.timer.TimerQueue
import dev.simpletimer.util.Log
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Shardの開始に対応する
 *
 */
object Ready : ListenerAdapter() {
    private var count = 0

    /**
     * JDA(Shardも含む)起動時に呼び出される
     *
     * @param event [ReadyEvent]イベント
     */
    override fun onReady(event: ReadyEvent) {
        //コンフィグを取得
        val config = SimpleTimer.instance.dataContainer.config

        //サーバーの一覧を確認
        config.loggingChannels.map { it.value }.forEach guildID@{
            //チャンネルを取得
            val channel = event.jda.getTextChannelById(it) ?: return@guildID
            //ログを出力するチャンネルに追加
            Log.logChannels.add(channel)
        }

        Log.sendLog("Shardを起動しました　${++count}/${SimpleTimer.instance.dataContainer.config.shardsCount.value}")

        //DB上のタイマー再開などをする
        event.jda.guilds
            .map { guild ->
                // 終了済みのまま残っているタイマーを掃除
                TimerDataTransaction.deleteFinishedTimerData(guild)
                // GuildにあるTimerDataを取得
                TimerDataTransaction.getTimerData(guild)
            }
            .forEach {
                it.filterNot { timerData ->
                    //終了済みのタイマーは飛ばす
                    timerData.timerServiceData.isFinish ||
                            // チャンネルを取得できなかったタイマーは飛ばす
                            timerData.channel.isFailure
                }.forEach { timerData ->
                    try {
                        // Timerのインスタンスを復元
                        Timer(timerData)
                    } catch (e: Exception) {
                        Log.sendLog("タイマーの復元に失敗しました: ${e.stackTraceToString()}", true)
                    }
                }
            }

        //キューのDBを掃除します。
        TimerQueueTransaction.cleanup()
        //DBのキューを登録する
        event.jda.guilds.map { TimerQueueTransaction.getQueue(it) }.forEach {
            it.forEach { (channel, number, queue) ->
                TimerQueue.registerTimerQueue(channel, number, queue)
            }
        }
    }
}