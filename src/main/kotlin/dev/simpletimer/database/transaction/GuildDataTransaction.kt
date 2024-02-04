package dev.simpletimer.database.transaction

import dev.simpletimer.data.serializer.AudioChannelSerializer
import dev.simpletimer.data.serializer.GuildMessageChannelSerializer
import dev.simpletimer.data.serializer.GuildSerializer
import dev.simpletimer.data.serializer.RoleSerializer
import dev.simpletimer.database.Connector
import dev.simpletimer.database.data.GuildData
import dev.simpletimer.database.table.GuildDataTable
import dev.simpletimer.extension.decode
import dev.simpletimer.extension.decodeList
import dev.simpletimer.extension.encode
import dev.simpletimer.extension.encodeCollection
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * [GuildDataTable]に対するトランザクション
 *
 */
object GuildDataTransaction {
    /**
     * ギルドのデータを挿入する
     *
     * @param guildId ギルドのId
     * @param guildData [GuildData]
     */
    fun insertGuildData(guildId: Long, guildData: GuildData) {
        Connector.connect()

        transaction {
            //INSERT
            GuildDataTable.insert {
                it[discordGuildId] = guildId
                it[ttsTiming] = guildData.ttsTiming
                it[finishTTS] = guildData.finishTTS
                it[mention] = guildData.mention
                it[mentionTiming] = guildData.mentionTiming
                it[vcMentionTargets] =
                    guildData.vcMentionTargets.encodeCollection(AudioChannelSerializer)
                it[roleMentionTargets] = guildData.roleMentionTargets.encodeCollection(RoleSerializer)
                it[diceMode] = guildData.diceMode
                it[diceBot] = guildData.diceBot
                it[list] = guildData.list
                it[listTargetChannel] = guildData.listTargetChannel.encode(GuildMessageChannelSerializer)
                it[listSync] = guildData.listSync
                it[syncTarget] = guildData.syncTarget.encode(GuildSerializer)
                it[audio] = guildData.audio
                it[needAudioAnnounce] = guildData.needAudioAnnounce
                it[lang] = guildData.lang
            }
        }
    }

    /**
     * ギルドのデータを更新する
     *
     * @param guildId ギルドのId
     * @param guildData [GuildData]
     */
    fun updateGuildData(guildId: Long, guildData: GuildData) {
        Connector.connect()

        //データが無いときは挿入
        if (transaction {
                GuildDataTable.selectAll().where { GuildDataTable.discordGuildId eq guildId }.limit(1).firstOrNull()
            } == null) {
            insertGuildData(guildId, guildData)
            return
        }

        transaction {
            //UPDATE
            GuildDataTable.update({ GuildDataTable.discordGuildId eq guildId }) {
                it[discordGuildId] = guildId
                it[ttsTiming] = guildData.ttsTiming
                it[finishTTS] = guildData.finishTTS
                it[mention] = guildData.mention
                it[mentionTiming] = guildData.mentionTiming
                it[vcMentionTargets] = guildData.vcMentionTargets.encodeCollection(AudioChannelSerializer)
                it[roleMentionTargets] = guildData.roleMentionTargets.encodeCollection(RoleSerializer)
                it[diceMode] = guildData.diceMode
                it[diceBot] = guildData.diceBot
                it[list] = guildData.list
                it[listTargetChannel] = guildData.listTargetChannel.encode(GuildMessageChannelSerializer)
                it[listSync] = guildData.listSync
                it[syncTarget] = guildData.syncTarget.encode(GuildSerializer)
                it[audio] = guildData.audio
                it[needAudioAnnounce] = guildData.needAudioAnnounce
                it[lang] = guildData.lang
            }
        }
    }

    /**
     * ギルドのデータを取得する
     *
     * @param guildId ギルドのId
     * @return [GuildData]?
     */
    fun getGuildData(guildId: Long): GuildData? {
        Connector.connect()

        return transaction {
            GuildDataTable.selectAll().where { GuildDataTable.discordGuildId eq guildId }.limit(1).firstOrNull()?.let {
                GuildData(
                    it[GuildDataTable.ttsTiming],
                    it[GuildDataTable.finishTTS],
                    it[GuildDataTable.mention],
                    it[GuildDataTable.mentionTiming],
                    it[GuildDataTable.vcMentionTargets].decodeList(AudioChannelSerializer).toMutableList(),
                    it[GuildDataTable.roleMentionTargets].decodeList(RoleSerializer).toMutableList(),
                    it[GuildDataTable.diceMode],
                    it[GuildDataTable.diceBot],
                    it[GuildDataTable.list],
                    it[GuildDataTable.listTargetChannel].decode(GuildMessageChannelSerializer),
                    it[GuildDataTable.listSync],
                    it[GuildDataTable.syncTarget].decode(GuildSerializer),
                    it[GuildDataTable.audio],
                    it[GuildDataTable.needAudioAnnounce],
                    it[GuildDataTable.lang]
                )
            }
        }
    }
}