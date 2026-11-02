package dev.simpletimer.listener

import dev.simpletimer.database.transaction.TimerMessageTransaction
import dev.simpletimer.extension.checkSimpleTimerPermission
import dev.simpletimer.timer.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

/**
 * リアクションの操作を行うクラス
 *
 */
object GenericMessageReaction : ListenerAdapter() {
    //各チャンネルのクールタイム用のセット
    private val coolTime: MutableSet<Long> = ConcurrentHashMap.newKeySet()

    /**
     * メッセージにリアクションがされた時に呼び出される
     *
     * @param event [GenericMessageReactionEvent] イベント
     */
    override fun onGenericMessageReaction(event: GenericMessageReactionEvent) {
        super.onGenericMessageReaction(event)

        //ユーザーの確認
        val user = event.user
        if (user != null && user.isBot) return

        //管理者権限か、必要な権限を確認
        if (!event.guildChannel.checkSimpleTimerPermission()) {
            return
        }

        // クールタイムの確認と確保を同時に行う
        // 既にクールタイム中なら終了
        val idLong = event.messageIdLong
        if (!coolTime.add(idLong)) return

        //処理を行ったか
        var handled = false
        try {
            //タイマーの取得
            val timerData = TimerMessageTransaction.getTimerDataByMessage(idLong) ?: return
            val timer = Timer.getTimerFromTimerDataId(timerData.timerDataId)

            //タイマーの確認
            if (timer != null) {
                //リアクションの確認・処理
                when (event.reaction.emoji.name) {
                    "1️⃣" -> timer.add(1 * 60)
                    "3️⃣" -> timer.add(3 * 60)
                    "5️⃣" -> timer.add(5 * 60)
                    "\uD83D\uDD1F" -> timer.add(10 * 60)
                    else -> return
                }
            }
            handled = true
        } finally {
            //処理しなかった場合はクールタイムを解除
            if (!handled) coolTime.remove(idLong)
        }

        //クールタイムの解除
        CoroutineScope(Dispatchers.Default).launch {
            try {
                delay(100.milliseconds)
                coolTime.remove(idLong)
            } catch (_: InterruptedException) {
                coolTime.remove(idLong)
            }
        }
    }
}