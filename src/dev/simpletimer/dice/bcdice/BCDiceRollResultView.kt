package dev.simpletimer.dice.bcdice

import dev.simpletimer.bcdice_client.BCDiceClient
import dev.simpletimer.extension.equalsIgnoreCase
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import dev.simpletimer.util.Log
import net.dv8tion.jda.api.entities.Guild

/**
 * BCDiceのロール結果の表示用メッセージを作成する
 */
object BCDiceRollResultView {
    /**
     * BCDiceのダイスを振って、結果のメッセージを作成する
     *
     * @param guild [Guild] ダイスを振るギルド
     * @param command [String] ダイスの内容
     * @return 結果のメッセージ 失敗時はnull
     */
    fun create(guild: Guild, command: String): String? {
        var runCommand = command

        //スポイラーによるシークレットダイスを確認
        if (command.contains("||") && !command.substring(0, 1).equalsIgnoreCase("s")) {
            runCommand = "s$command".replace("||", "")
        }

        //ギルドのデータから設定されているダイスボットを取得
        val id = guild.getGuildData().diceBot

        //コマンドを実行
        val result = try {
            BCDiceClient.instance.roll(id, runCommand)
        } catch (e: Exception) {
            Log.sendLog(e.stackTraceToString(), true)
            return null
        }

        //失敗時はnullを返す
        if (!result.ok) {
            return null
        }

        //メッセージを構築して返す
        return if (result.secret) {
            //シークレットダイス
            "${guild.getLang().dice.secret}\n||${result.text}||"
        } else {
            if (result.success || result.critical) {
                //成功
                "```diff\n+ ${result.text}```"
            } else if (result.failure || result.fumble) {
                //失敗
                "```diff\n- ${result.text}```"
            } else {
                //その他
                result.text
            }
        }
    }
}
