/*
 * MIT License
 *
 * Copyright (c) 2025 Simeon L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lol.simeon.humanoid.console

import org.fusesource.jansi.Ansi
import java.awt.Color
import java.lang.String.valueOf
import java.util.regex.Pattern

enum class ConsoleColor(val nameColor: String, val index: Char, private val ansi: String) {
    BLACK("black", '0', Ansi.ansi().reset().fg(Ansi.Color.BLACK).toString()),
    DARK_BLUE("dark_blue", '1', Ansi.ansi().reset().fg(Ansi.Color.BLUE).toString()),
    GREEN("green", '2', Ansi.ansi().reset().fg(Ansi.Color.GREEN).toString()),
    CYAN("cyan", '3', Ansi.ansi().reset().fg(Ansi.Color.CYAN).toString()),
    DARK_RED("dark_red", '4', Ansi.ansi().reset().fg(Ansi.Color.RED).toString()),
    PURPLE("purple", '5', Ansi.ansi().reset().fg(Ansi.Color.MAGENTA).toString()),
    ORANGE("orange", '6', Ansi.ansi().reset().fg(Ansi.Color.YELLOW).toString()),
    GRAY("gray", '7', Ansi.ansi().reset().fg(Ansi.Color.WHITE).toString()),
    DARK_GRAY("dark_gray", '8', Ansi.ansi().reset().fg(Ansi.Color.BLACK).bold().toString()),
    BLUE("blue", '9', Ansi.ansi().reset().fg(Ansi.Color.BLUE).bold().toString()),
    LIGHT_GREEN("light_green", 'a', Ansi.ansi().reset().fg(Ansi.Color.GREEN).bold().toString()),
    AQUA("aqua", 'b', Ansi.ansi().reset().fg(Ansi.Color.CYAN).bold().toString()),
    RED("red", 'c', Ansi.ansi().reset().fg(Ansi.Color.RED).bold().toString()),
    PINK("pink", 'd', Ansi.ansi().reset().fg(Ansi.Color.MAGENTA).bold().toString()),
    YELLOW("yellow", 'e', Ansi.ansi().reset().fg(Ansi.Color.YELLOW).bold().toString()),
    WHITE("white", 'f', Ansi.ansi().reset().fg(Ansi.Color.WHITE).bold().toString()),
    OBFUSCATED("obfuscated", 'k', Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString()),
    BOLD("bold", 'l', Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString()),
    STRIKETHROUGH("strikethrough", 'm', Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString()),
    UNDERLINE("underline", 'n', Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString()),
    ITALIC("italic", 'o', Ansi.ansi().a(Ansi.Attribute.ITALIC).toString()),
    DEFAULT("default", 'r', Ansi.ansi().reset().toString());


    override fun toString(): String {
        return this.ansi
    }

    companion object {
        private val VALUES = ConsoleColor.values()
        private const val LOOKUP: String = "0123456789abcdefklmnor"
        private const val RGB_ANSI: String = "\u001B[38;2;%d;%d;%dm"

        fun toColouredString(triggerChar: Char, text: String): String {
            val content = convertRGBColors(triggerChar, text)

            var breakIndex = content.length - 1
            var i = 0
            while (i <= breakIndex) {
                if (content[i] == triggerChar) {
                    val format = LOOKUP.indexOf(content[i + 1])
                    if (format != -1) {
                        val ansiCode = VALUES[format].ansi
                        content.delete(i, i + 2).insert(i, ansiCode)
                        breakIndex += ansiCode.length - 2
                    }
                }
                i++
            }

            return content.toString()
        }

        private fun convertRGBColors(trigger: Char, input: String): StringBuffer {
            val matcher = Pattern.compile("$trigger#([\\da-fA-F]){6}").matcher(input)
            val stringBuffer = StringBuffer()

            while (matcher.find()) {
                val temp: String =
                    matcher.group().replace(valueOf(trigger), "")
                val color = Color.decode(temp)
                matcher.appendReplacement(
                    stringBuffer,
                    String.format(RGB_ANSI, color.red, color.green, color.blue)
                )
            }

            matcher.appendTail(stringBuffer)
            return stringBuffer

        }
    }
}