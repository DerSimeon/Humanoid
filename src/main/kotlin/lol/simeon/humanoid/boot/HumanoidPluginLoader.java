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

package lol.simeon.humanoid.boot;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.gremlin.runtime.DependencyCache;
import xyz.jpenilla.gremlin.runtime.DependencyResolver;
import xyz.jpenilla.gremlin.runtime.DependencySet;
import xyz.jpenilla.gremlin.runtime.platformsupport.PaperClasspathAppender;

import java.nio.file.Path;
import java.util.Set;

/**
 * HumanoidPluginLoader
 * Used to load the plugin's dependencies and inject them into the classpath.
 */
@SuppressWarnings({"UnstableApiUsage", "unused"})
public class HumanoidPluginLoader implements PluginLoader {

    /**
     * load the plugin's dependencies and inject them into the classpath.
     *
     * @param classpathBuilder the classpath builder
     */
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        new PaperClasspathAppender(classpathBuilder).append(resolve(classpathBuilder.getContext().getDataDirectory().getParent().resolve("../libraries")));
    }
    
    private Set<Path> resolve(Path cacheDir) {
        DependencySet dependencySet = DependencySet.readFromClasspathResource(this.getClass().getClassLoader(), "humanoid-dependencies.txt");
        DependencyCache dependencyCache = new DependencyCache(cacheDir);
        Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
        Set<Path> result;
        try(DependencyResolver resolver = new DependencyResolver(logger)) {
            result = resolver.resolve(dependencySet, dependencyCache).jarFiles();
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve dependencies", e);
        }
        return result;
        
    }
}