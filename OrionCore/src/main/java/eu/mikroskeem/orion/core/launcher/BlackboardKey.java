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

package eu.mikroskeem.orion.core.launcher;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.TypesafeMap;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Blackboard keys
 *
 * @author Mark Vainomaa
 */
public final class BlackboardKey {
    private BlackboardKey() {}

    private static <T> TypesafeMap.Key<T> create(@NonNull String name, @NonNull Class<T> type) {
        return TypesafeMap.Key.getOrCreate(() -> Launcher.INSTANCE.blackboard(), name, type).get();
    }

    public static final TypesafeMap.Key<List> ORIGINAL_ARGUMENTS = create("orion.launch.arguments", List.class);
    public static final TypesafeMap.Key<String> LAUNCH_TARGET = create("orion.launch.target", String.class);
    public static final TypesafeMap.Key<Path> LIBRARIES_PATH = create("orion.path.libraries", Path.class);
    public static final TypesafeMap.Key<Path> MODS_PATH = create("orion.path.mods", Path.class);
    public static final TypesafeMap.Key<Path> MOD_CONFIGS_PATH = create("orion.path.modconfigs", Path.class);

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T get(TypesafeMap.@NonNull Key<T> key) {
        return Launcher.INSTANCE.blackboard().get(key).orElseThrow(() -> new IllegalStateException("Key '" + key.name() + "' is not set"));
    }

    @NonNull
    public static <T> T getOr(TypesafeMap.@NonNull Key<T> key, @NonNull Supplier<T> def) {
        return Launcher.INSTANCE.blackboard().computeIfAbsent(key, (k) -> requireNonNull(def.get(), "Value is null"));
    }

    public static <T> void set(TypesafeMap.@NonNull Key<T> key, @NonNull T value) {
        getOr(key, () -> value);
    }
}