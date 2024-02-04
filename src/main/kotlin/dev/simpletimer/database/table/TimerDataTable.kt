package dev.simpletimer.database.table

import dev.simpletimer.timer.Timer
import org.jetbrains.exposed.sql.Table

/**
 * 現在動いているタイマーのテーブル
 *
 */
object TimerDataTable : Table("timer_data") {
    val timerDataId = long("timer_data_id").autoIncrement()

    //タイマーの基本データ
    val channel = text("channel")
    val number = enumeration<Timer.Number>("number")
    val displayMessageBase = text("timer_message_base").nullable()

    //ギルドのID
    val guildId = long("guild_id")

    //動作状況
    val seconds = integer("seconds")
    val isStarted = bool("is_started")
    val isMove = bool("is_move")
    val isFinish = bool("is_finish")
    val startMilliTime = long("start_milli_time")
    val adjustTime = long("adjust_time")
    val stopTime = long("stop_time")

    override val primaryKey = PrimaryKey(timerDataId)
}