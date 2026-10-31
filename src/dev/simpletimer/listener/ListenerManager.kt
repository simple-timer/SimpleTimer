package dev.simpletimer.listener

/**
 * リスナーを取りまとめるクラス
 */
object ListenerManager {
    //リスナーのSet
    val listeners = arrayOf(
        ButtonInteraction,
        ChannelDelete,
        CommandAutoCompleteInteraction,
        GenericMessageReaction,
        GuildLeave,
        GuildVoiceUpdate,
        MessageDelete,
        ModalInteraction,
        Ready,
        SelectMenuInteraction,
        SlashCommandInteraction
    )
}