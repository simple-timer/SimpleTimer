package dev.simpletimer.extension

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json


/**
 * [Collection]<T>の拡張です。
 * Jsonのテキストにエンコードします。
 *
 * @param T エンコード対象の型です。
 * @param kSerializer 使用する[KSerializer]です。
 * @return Jsonのテキストです。
 * @author mqrimo
 */
fun <T> Collection<T>.encodeCollection(kSerializer: KSerializer<T>): String {
    if (this.isEmpty()) return ""
    return this.joinToString(",") { it.encode(kSerializer) }
}

/**
 * 任意の型Tにの拡張です。
 * Jsonのテキストにエンコードします。
 *
 * @param T エンコード対象の型です。
 * @param kSerializer 使用する[KSerializer]です。
 * @return Jsonのテキストです。
 * @author mqrimo
 */
fun <T> T.encode(kSerializer: KSerializer<T>): String {
    return Json.Default.encodeToString(kSerializer, this)
}

/**
 * Stringの拡張です。
 * Jsonのテキストから[List]<T>にデコードします。
 *
 * @param T エンコード対象のリストの要素の型です。
 * @param kSerializer 使用する[KSerializer]です。
 * @return デコード結果の[List]です。
 * @author mqrimo
 */
fun <T> String.decodeList(kSerializer: KSerializer<T>): List<T> {
    if (this == "") return emptyList()
    return this.split(",").map { it.decode(kSerializer) }
}

/**
 * Stringの拡張です
 * Jsonのテキストから任意の型Tにデコードします。
 *
 * @param T エンコード対象の型です。
 * @param kSerializer 使用する[KSerializer]です。
 * @return デコードの結果のTです
 * @author mqrimo
 */
fun <T> String.decode(kSerializer: KSerializer<T>): T {
    return Json.Default.decodeFromString(kSerializer, this)
}