/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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

package eu.mikroskeem.orion.core;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static eu.mikroskeem.orion.core.OrionTweakClass.OrionTweakerData.launchTarget;
import static eu.mikroskeem.orion.core.OrionTweakClass.OrionTweakerData.originalArguments;


/**
 * Orion Tweak class
 *
 * @author Mark Vainomaa
 */
public final class OrionTweakClass implements ITweaker {
    private static final Logger logger = LogManager.getLogger("OrionTweakClass");

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        /* Set up Orion core */
        OrionCore.INSTANCE.setupCore(classLoader);

        /* Set up mods */
        try {
            OrionCore.INSTANCE.setupMods(classLoader, Paths.get("./mods"));
        } catch (IOException e) {
            logger.error("Failed to load mods", e);
        }

        /* Set up transformers */
        OrionCore.INSTANCE.setupTransformers(classLoader);
    }

    @Override
    public String getLaunchTarget() {
        return launchTarget;
    }

    @Override
    public String[] getLaunchArguments() {
        return originalArguments.toArray(new String[originalArguments.size()]);
    }

    @Override
    public void acceptOptions(List<String> args) {}

    /** Tweak class data store */
    public static class OrionTweakerData {
        public final static List<String> originalArguments = new ArrayList<>();
        public static String launchTarget;
        public static Path librariesPath;
    }
}
