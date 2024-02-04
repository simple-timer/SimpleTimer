package dev.simpletimer.database.table

import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.data.enum.Mention
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.data.lang.Lang
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

/**
 * ギルドのデータの[Table]
 *
 */
object GuildDataTable : Table("guild_data") {
    //自動採番キー
    private val guildDataId = long("guild_data_id").autoIncrement()

    //DiscordのギルドのID
    val discordGuildId = long("discord_guild_id")

    //データ本体
    val ttsTiming = enumeration<NoticeTiming>("tts_timing").default(NoticeTiming.LV0)
    val finishTTS = text("finish_tts").default("x番目のタイマーが終了しました")
    val mention = enumeration<Mention>("mention").default(Mention.VC)
    val mentionTiming = enumeration<NoticeTiming>("mention_timing").default(NoticeTiming.LV2)
    val vcMentionTargets = text("vc_mention_targets").default("")
    val roleMentionTargets = text("role_mention_targets").default("")
    val diceMode = enumeration<DiceMode>("dice_mode").default(DiceMode.Default)
    val diceBot = text("dice_bot").default("DiceBot")
    val list = json<LinkedHashMap<String, String>>("list", Json.Default).default(linkedMapOf())
    val listTargetChannel = text("list_target_channel")
    val listSync = bool("list_sync").default(false)
    val syncTarget = text("sync_target")
    val audio = text("audio").default("Voice")
    val needAudioAnnounce = bool("need_audio_announce").default(true)
    val lang = enumeration<Lang>("lang")


    override val primaryKey = PrimaryKey(guildDataId)
}