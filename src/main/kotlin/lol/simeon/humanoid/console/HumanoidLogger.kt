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

import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter


public class HumanoidLogger {

    public companion object {
        private val logger: Logger = Logger.getLogger("HumanoidLogger")

        init {
            val handler = ConsoleHandler()
            logger.useParentHandlers = false
            handler.level = Level.ALL
            logger.addHandler(handler)
            logger.level = Level.ALL
        }

        public fun info(var1: String) {
            print(format("INFO", "&7$var1&r"))
        }

        public fun warn(var1: String) {
            print(format("WARNING", "&6$var1&r"))
        }

        public fun error(var1: String) {
            print(format("SEVERE", "&4$var1&r"))
        }

        public fun debug(var1: String?) {
            print(format("DEBUG", var1!!))
        }

        public fun success(var1: String) {
            print(format("FINEST", "&a$var1&r"))
        }

        private fun print(var1: String) {
            println(var1)
        }


        private fun format(level: String, message: String): String {
            val simpleFormatter = SimpleFormatter()
            return ConsoleColor.toColouredString(
                '&', simpleFormatter.format(
                    LogRecord(
                        Level.parse(level.uppercase(Locale.getDefault())),
                        message
                    )
                )
            ).trimEnd()
        }
        
        

    }
}