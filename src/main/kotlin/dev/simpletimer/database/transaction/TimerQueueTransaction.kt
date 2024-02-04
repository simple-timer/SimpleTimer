package dev.simpletimer.database.transaction

import dev.simpletimer.data.serializer.GuildMessageChannelSerializer
import dev.simpletimer.data.serializer.GuildSerializer
import dev.simpletimer.database.Connector
import dev.simpletimer.database.table.TimerQueueTable
import dev.simpletimer.extension.decode
import dev.simpletimer.extension.encode
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

/**
 * [TimerQueueTable]用のトランザクション
 */
object TimerQueueTransaction {
    /**
     * Queueの情報を挿入・更新する
     *
     * @param channel [GuildMessageChannel]
     * @param number [Timer.Number]
     * @param queue キューの内容 [List]<[Int]>
     */
    fun upsertQueue(channel: GuildMessageChannel, number: Timer.Number, queue: List<Int> = mutableListOf()) {
        Connector.connect()

        //UPSERT
        transaction {
            TimerQueueTable.upsert {
                it[TimerQueueTable.channel] = Result.success(channel).encode(GuildMessageChannelSerializer)
                it[TimerQueueTable.number] = number
                it[TimerQueueTable.queue] = queue
                it[guild] = Result.success(channel.guild).encode(GuildSerializer)
            }
        }
    }

    /**
     * [GuildMessageChannel]と[Timer.Number]からキューを取得する
     *
     * @param channel [GuildMessageChannel]
     * @param number [Timer.Number]
     * @return キューの内容 [List]<[Int]>
     */
    fun getQueue(channel: GuildMessageChannel, number: Timer.Number): List<Int> {
        Connector.connect()

        //SELECT
        return transaction {
            TimerQueueTable.selectAll().where {
                TimerQueueTable.channel.eq(
                    Result.success(channel).encode(GuildMessageChannelSerializer)
                ) and TimerQueueTable.number.eq(number)
            }.firstOrNull()?.let {
                    return@transaction it[TimerQueueTable.queue]
                } ?: return@transaction mutableListOf()
        }
    }

    /**
     * [Guild]上のキューを[Triple]ですべて取得する
     *
     * @param guild [Guild]
     * @return [List]<[Triple]<[GuildMessageChannel], [Timer.Number], [List]<[Int]>>>
     */
    fun getQueue(guild: Guild): List<Triple<GuildMessageChannel, Timer.Number, List<Int>>> {
        Connector.connect()

        return transaction {
            TimerQueueTable.selectAll().where(TimerQueueTable.guild.eq(Result.success(guild).encode(GuildSerializer)))
                .mapNotNull {
                    it[TimerQueueTable.channel].decode(GuildMessageChannelSerializer).getOrNull()?.let { channel ->
                        Triple(channel, it[TimerQueueTable.number], it[TimerQueueTable.queue])
                    }
            }
        }
    }

    /**
     * クリア
     *
     * @param channel [GuildMessageChannel]
     * @param number [Timer.Number]
     */
    fun clear(channel: GuildMessageChannel, number: Timer.Number) {
        Connector.connect()

        //DELETE
        transaction {
            TimerQueueTable.deleteWhere {
                TimerQueueTable.channel.eq(
                    Result.success(channel).encode(GuildMessageChannelSerializer)
                ) and TimerQueueTable.number.eq(number)
            }
        }
    }
}