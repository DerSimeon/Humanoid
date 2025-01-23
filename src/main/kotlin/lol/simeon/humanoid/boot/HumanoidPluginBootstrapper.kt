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

package lol.simeon.humanoid.boot

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import lol.simeon.humanoid.Humanoid
import org.bukkit.plugin.java.JavaPlugin
import lol.simeon.humanoid.console.HumanoidLogger

@Suppress("UnstableApiUsage", "unused")
public class HumanoidPluginBootstrapper : PluginBootstrap {
    
    override fun bootstrap(bootstrapContext: BootstrapContext) {
        System.setProperty(
            "java.util.logging.SimpleFormatter.format", "[%4$-7s] %5\$s %n"
        );
        HumanoidLogger.warn("Bootstrapping Humanoid version " + bootstrapContext.pluginMeta.version)
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        HumanoidLogger.info("Instantiating Humanoid instance.")
        return Humanoid()
    }
}