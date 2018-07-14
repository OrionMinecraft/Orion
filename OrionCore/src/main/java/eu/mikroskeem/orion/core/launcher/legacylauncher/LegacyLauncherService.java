/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.orion.core.launcher.legacylauncher;

import eu.mikroskeem.orion.api.bytecode.OrionTransformer;
import eu.mikroskeem.orion.core.launcher.AbstractLauncherService;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class LegacyLauncherService extends AbstractLauncherService {
    private Map<Class<? extends OrionTransformer>, Class<? extends IClassTransformer>> convertedTransformers = new HashMap<>();

    @NotNull
    @Override
    public Map<String, Object> getBlackBoard() {
        return Launch.blackboard;
    }

    @Override
    public void registerTransformer(@NotNull Class<? extends OrionTransformer> transformer) {
        Launch.classLoader.registerTransformer(
                convertedTransformers.computeIfAbsent(transformer, ClassTransformerConverter::convert).getName()
        );
    }

    @NotNull
    @Override
    public Set<String> getClassLoaderExclusions() {
        return Launch.classLoader.getClassLoaderExclusions();
    }

    @Override
    @NotNull
    public ClassLoader getClassLoader() {
        return Launch.classLoader;
    }

    @Override
    public void addURLToClassLoader(@NotNull URL url) {
        Launch.classLoader.addURL(url);
    }
}
