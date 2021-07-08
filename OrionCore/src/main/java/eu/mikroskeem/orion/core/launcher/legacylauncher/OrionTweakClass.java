/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2020 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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

import eu.mikroskeem.orion.core.OrionCore;
import eu.mikroskeem.orion.core.launcher.BlackboardKey;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.List;

import static eu.mikroskeem.orion.core.launcher.BlackboardKey.LAUNCHER_SERVICE;


/**
 * Orion Tweak class
 *
 * @author Mark Vainomaa
 */
public final class OrionTweakClass implements ITweaker {
    private static final Logger logger = LogManager.getLogger("OrionTweakClass");

    @Override
    public void injectIntoClassLoader(@NonNull LaunchClassLoader classLoader) {
        /* Set up Orion core */
        OrionCore.INSTANCE.setupCore();

        /* Set up mods */
        try {
            OrionCore.INSTANCE.setupMods(BlackboardKey.get(LAUNCHER_SERVICE), BlackboardKey.get(BlackboardKey.MODS_PATH));
        } catch (IOException e) {
            logger.error("Failed to load mods", e);
        }

        /* Set up transformers */
        OrionCore.INSTANCE.setupTransformers();
    }

    @NonNull
    @Override
    public String getLaunchTarget() {
        return BlackboardKey.get(BlackboardKey.LAUNCH_TARGET);
    }

    @NonNull
    @Override
    public String[] getLaunchArguments() {
        List<String> arguments = BlackboardKey.get(BlackboardKey.ORIGINAL_ARGUMENTS);
        return arguments.toArray(new String[0]);
    }

    @Override
    public void acceptOptions(@NonNull List<String> args) {}
}
