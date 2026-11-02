package dev.simpletimer.timer

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.database.transaction.TimerDataTransaction
import dev.simpletimer.database.transaction.TimerQueueTransaction
import dev.simpletimer.extension.getTimerList
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * キュー周りの処理
 *
 * @property channel 対象のチャンネル
 * @property number 対象の[Timer.Number]
 *
 * @param queue キューの内容
 */
class TimerQueue(val channel: GuildMessageChannel, val number: Timer.Number, queue: List<Int> = mutableListOf()) :
    TimerService.TimerListener {
    companion object {
        //キューのマップ
        private val timerQueues = ConcurrentHashMap<Long, ConcurrentHashMap<Timer.Number, TimerQueue>>()

        /**
         * チャンネルとギルドとナンバーからタイマーのキューを取得する
         *
         * @param channel [GuildMessageChannel]
         * @param number [Timer.Number]
         * @return [TimerQueue]を返す
         */
        fun getTimerQueue(channel: GuildMessageChannel, number: Timer.Number): TimerQueue {
            return timerQueues.getOrPut(channel.idLong) { ConcurrentHashMap() }
                .getOrPut(number) { TimerQueue(channel, number) }
        }

        /**
         * キャッシュ済みのタイマーのキューを取得する
         *
         * @param channel [GuildMessageChannel]
         * @param number [Timer.Number]
         * @return [TimerQueue]、存在しない場合はnull
         */
        fun getTimerQueueOrNull(channel: GuildMessageChannel, number: Timer.Number): TimerQueue? {
            return timerQueues[channel.idLong]?.get(number)
        }

        /**
         * タイマーキューを登録する
         *
         * @param channel [GuildMessageChannel]
         * @param number [Timer.Number]
         * @param queue キューの内容
         */
        fun registerTimerQueue(channel: GuildMessageChannel, number: Timer.Number, queue: List<Int>) {
            // mapを取得
            val queues = timerQueues.getOrPut(channel.idLong) { ConcurrentHashMap() }

            // 既にインスタンスがあるか
            val existing = queues[number]
            if (existing != null) {
                // ない場合、新しいキューを作成
                TimerQueueTransaction.upsertQueue(channel, number, queue)
                existing.attachToCurrentTimer()
                return
            }

            //mapに追加
            queues[number] = TimerQueue(channel, number, queue)
        }
    }

    init {
        //キューをDBに登録します。
        TimerQueueTransaction.upsertQueue(channel, number, queue)

        //タイマーのインスタンスを取得して、リスナーとして登録します。
        attachToCurrentTimer()
    }

    /**
     * 現在動いているタイマーのリスナーとして登録する
     *
     */
    private fun attachToCurrentTimer() {
        //タイマーのインスタンスを取得して、リスナーとして登録します。
        TimerDataTransaction.getTimerData(channel, number)?.timerDataId?.let { timerDataId ->
            Timer.getTimerFromTimerDataId(timerDataId)?.timerService?.registerListener(this)
        }
    }

    /**
     * キューを追加する
     *
     * @param seconds 秒
     */
    fun addTimerQueue(seconds: Int) {
        //チャンネルのタイマーを取得する
        val channelTimers = channel.getTimerList()

        //タイマーが動いているかを確認
        if (channelTimers.any { it.timerData.number == number }) {
            //キューに追加
            TimerQueueTransaction.getQueue(channel, number).toMutableList().apply {
                add(seconds)
            }.let {
                TimerQueueTransaction.upsertQueue(channel, number, it)
            }
        } else {
            //タイマーを作成し、リスナーとして登録してから開始する
            Timer(channel, number, seconds)
                .apply { timerService.registerListener(this@TimerQueue) }.start()
        }
    }

    /**
     * キューを空にする
     *
     */
    fun clearQueue() {
        TimerQueueTransaction.clear(channel, number)
    }

    /**
     * Indexからキューを削除
     *
     * @param index インデックス
     */
    fun removeQueueIndex(index: Int) {
        //キューを取得
        TimerQueueTransaction.getQueue(channel, number).toMutableList().apply {
            //Indexを指定して削除
            removeAt(index)
        }.let {
            //削除後のキューの大きさを確認
            if (it.isEmpty()) {
                //消し飛ばす
                clearQueue()
            } else {
                //キューの内容を更新
                TimerQueueTransaction.upsertQueue(channel, number, it)
            }
        }
    }

    /**
     * キューを取得
     *
     * @return [List]<[Int]>
     */
    fun getQueue(): List<Int> {
        return TimerQueueTransaction.getQueue(channel, number)
    }

    override fun onFinish(check: Boolean) {
        //すでに終了している場合は何もしない
        if (!check) return

        val queue = TimerQueueTransaction.getQueue(channel, number).toMutableList()

        //キューがあるかを確認
        if (queue.isEmpty()) return

        //タイマーを作成し、リスナーとして登録してから開始させる
        Timer(channel, number, queue[0])
            .apply { timerService.registerListener(this@TimerQueue) }
            .start()

        //キューを削除
        removeQueueIndex(0)
    }


    /**
     * キューの埋め込みを作成する
     *
     * @return 作成した[MessageEmbed]
     */
    fun getQueueEmbed(langData: LangData): MessageEmbed {
        val embed = EmbedBuilder()

        //タイトルを設定
        embed.setTitle(langData.timer.queueNumber.langFormat(number.number))

        //キューを確認
        getQueue().withIndex().forEach { (index, totalSeconds) ->
            //フィールドを追加
            embed.addField(
                "${index + 1}",
                "${langData.timer.minutes.langFormat(totalSeconds / 60)}${langData.timer.seconds.langFormat(totalSeconds % 60)}",
                true
            )
        }

        //作って返す
        return embed.build()
    }


    override fun onStart() {
        //何もしない
    }

    override fun onStop(check: Boolean) {
        //何もしない
    }

    override fun onRestart(check: Boolean) {
        //何もしない
    }

    override fun onEnd(check: Boolean) {
        //何もしない
    }

    override fun onAdd(seconds: Int) {
        //何もしない
    }

    override fun onUpdate() {
        //何もしない
    }
}