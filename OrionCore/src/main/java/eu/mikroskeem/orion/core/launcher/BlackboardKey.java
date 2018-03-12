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

package eu.mikroskeem.orion.core.launcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Blackboard keys
 *
 * @author Mark Vainomaa
 */
public enum BlackboardKey {
    LAUNCHER_SERVICE("orion.launch.service", AbstractLauncherService.class, false),

    ORIGINAL_ARGUMENTS("orion.launch.arguments", List.class),
    LAUNCH_TARGET("orion.launch.target", String.class),

    LIBRARIES_PATH("orion.path.libraries", Path.class, false),
    MODS_PATH("orion.path.mods", Path.class, false),
    MOD_CONFIGS_PATH("orion.path.modconfigs", Path.class, false),

    AT_URLS("orion.at.urls", List.class, ArrayList<URL>::new)
    ;

    private final String key;
    private final Class<?> type;
    private final boolean mutable;
    @Nullable private final Supplier<?> initializer;

    BlackboardKey(@NotNull String key, @NotNull Class<?> type) {
        this.key = key;
        this.type = type;
        this.mutable = true;
        this.initializer = null;
    }

    BlackboardKey(@NotNull String key, @NotNull Class<?> type, boolean mutable) {
        this.key = key;
        this.type = type;
        this.mutable = mutable;
        this.initializer = null;
    }

    BlackboardKey(@NotNull String key, @NotNull Class<?> type, @Nullable Supplier<?> initializer) {
        this.key = key;
        this.type = type;
        this.mutable = true;
        this.initializer = initializer;
    }

    public static Map<String, Object> blackboard = null;

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T get(@NotNull BlackboardKey key) {
        if(key.initializer != null)
            return (T) getOr(key, key.initializer);
        return actualGet(key);
    }

    @NotNull
    public static <T> T getOr(@NotNull BlackboardKey key, @NotNull Supplier<T> def) {
        try {
            return actualGet(key);
        } catch (NullPointerException e) {
            T value = requireNonNull(def.get(), "Supplier should not return null!");
            set(key, value);
            return value;
        }
    }

    public static <T> void set(@NotNull BlackboardKey key, @NotNull T value) {
        if(blackboard.containsKey(key.key) && !key.mutable)
            throw new IllegalArgumentException("Key " + key.key + " is not mutable");
        blackboard.put(key.key, requireNonNull(key.type.cast(value), "Value is null"));
    }

    public static void unset(@NotNull BlackboardKey key) {
        if(!key.mutable) throw new IllegalArgumentException("Key " + key.key + " is not mutable");
        blackboard.remove(key.key);
    }

    @SuppressWarnings("unchecked")
    private static <T> T actualGet(@NotNull BlackboardKey key) {
        Object value = requireNonNull(blackboard.get(key.key), "No value in blackboard with key " + key.key);
        return (T) key.type.cast(value);
    }
}
