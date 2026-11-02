package dev.simpletimer.listener

import dev.simpletimer.database.transaction.TimerDataTransaction
import dev.simpletimer.database.transaction.TimerQueueTransaction
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * チャンネルの削除に対応をするクラス
 *
 */
object ChannelDelete : ListenerAdapter() {
    /**
     * チャンネルを削除したときに呼び出される
     *
     * @param event [ChannelDeleteEvent]
     */
    override fun onChannelDelete(event: ChannelDeleteEvent) {
        // メッセージチャンネル以外対象外
        val channel = event.channel as? GuildMessageChannel ?: return

        //チャンネルのタイマーのデータを取得
        val timerDataList = TimerDataTransaction.getTimerData(channel)

        // メモリ上のタイマーを破棄
        timerDataList.forEach { Timer.getTimerFromTimerDataId(it.timerDataId)?.discard() }

        // DBから一括で削除
        TimerDataTransaction.deleteTimerData(timerDataList)

        //チャンネルのキューを削除
        TimerQueueTransaction.clear(channel)
    }
}