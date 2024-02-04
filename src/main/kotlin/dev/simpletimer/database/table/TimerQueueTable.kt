package dev.simpletimer.database.table

import dev.simpletimer.timer.Timer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

/**
 * タイマーのキュー用のテーブル
 */
object TimerQueueTable : Table("timer_queue") {
    //対象のチャンネル
    val channel = text("channel")

    //番号
    val number = enumeration<Timer.Number>("number")

    //キューの一覧
    val queue = json<List<Int>>("queue", Json.Default)

    //ギルド
    val guild = text("guild")

    override val primaryKey = PrimaryKey(channel, number)
}