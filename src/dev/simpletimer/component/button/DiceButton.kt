package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.dice.Dice
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

/**
 * ダイスを振るボタン
 *
 */
object DiceButton : ButtonManager.Button<String>("dice") {
    override fun run(event: ButtonInteractionEvent) {
        val buttonID = event.button.customId ?: return

        //ダイスのコマンドを取得
        val value = buttonID.replace("${name}:", "")

        //ダイスを振る
        Dice().roll(event, value, event.user)
    }

    override fun createButton(data: String, langData: LangData): Button {
        return Button.primary("${name}:$data", langData.component.button.roll)
            .withEmoji(Emoji.fromUnicode("\uD83C\uDFB2"))
    }
}