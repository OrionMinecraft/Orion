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

package eu.mikroskeem.orion.api.asset;

import eu.mikroskeem.orion.api.mod.ModInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Orion mod asset manager
 *
 * @author Mark Vainomaa
 */
public interface AssetManager {
    /**
     * Tries to get an asset from mod jar
     *
     * @param assetPath Asset path, like {@code config.cfg} or {@code lang/en_US.properties}
     * @return Instance of {@link InputStream} streaming asset contents or null, if asset couldn't be found
     */
    @Nullable
    InputStream getAsset(@NotNull String assetPath);

    /**
     * Copies asset from mod jar to path
     *
     * Does not overwrite destination file if already present
     *
     * @param destination Destination path
     * @param assetPath Asset path
     * @throws UncheckedIOException If asset copying fails either because of filesystem or resource finding error
     */
    void copyAsset(@NotNull Path destination, @NotNull String assetPath) throws UncheckedIOException;

    /**
     * Copies asset from mod jar to path
     *
     * @param destination Destination path
     * @param assetPath Asset path
     * @param overwriteExisting Whether to overwrite destination file if present
     * @throws UncheckedIOException If asset copying fails either because of filesystem or resource finding error
     */
    void copyAsset(@NotNull Path destination, @NotNull String assetPath, boolean overwriteExisting) throws UncheckedIOException;

    /**
     * Mod specific asset manager picker
     */
    interface ForMod {
        /**
         * Gets asset manager for mod specified with {@code modInfo}
         *
         * @param modInfo Instance of {@link ModInfo}
         * @return Mod specific {@link AssetManager}
         */
        @NotNull
        AssetManager forMod(@NotNull ModInfo modInfo);

        /**
         * Gets asset manager for mod specified with {@code modInfo}
         *
         * @param modId Mod id
         * @return Mod specific {@link AssetManager} or null, if no such mod exists
         */
        @Nullable
        AssetManager forMod(@NotNull String modId);
    }
}
