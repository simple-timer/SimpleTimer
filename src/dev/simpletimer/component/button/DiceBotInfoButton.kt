package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.dice.bcdice.GameSystemInfoView
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

/**
 * ダイスボットの情報を表示
 *
 */
object DiceBotInfoButton : ButtonManager.Button<Byte>("bot_info") {
    override fun run(event: ButtonInteractionEvent) {
        //Embedを作成して送信
        event.hook.sendMessageEmbeds(
            GameSystemInfoView.create(
                event.channel as GuildMessageChannel,
                event.guild!!
            )
        ).queue()
    }

    override fun createButton(data: Byte, langData: LangData): Button {
        //ボタンを作成して送信
        return Button.secondary(name, Emoji.fromUnicode("❓"))
    }
}