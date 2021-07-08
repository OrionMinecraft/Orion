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

package eu.mikroskeem.orion.core.mod;

import eu.mikroskeem.orion.api.asset.AssetManager;
import eu.mikroskeem.orion.core.launcher.BlackboardKey;
import eu.mikroskeem.orion.core.launcher.LauncherService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Mark Vainomaa
 */
final class ModAssetManager implements AssetManager {
    private final static String PATH_FORMAT = "/assets/%s/";

    private final String modAssetPath;

    ModAssetManager(@NonNull String modId) {
        this.modAssetPath = String.format(PATH_FORMAT, modId);
    }

    @NonNull
    private String formatAssetPath(@NonNull String assetPath) {
        return Paths.get(modAssetPath, assetPath).toString();
    }

    @NonNull
    private InputStream getAssetInternal(@NonNull String assetPath) throws IOException {
        String theAssetPath = formatAssetPath(assetPath);

        ClassLoader loader = BlackboardKey.<LauncherService>get(BlackboardKey.LAUNCHER_SERVICE).getClassLoader();
        InputStream assetStream;
        if((assetStream = loader.getResourceAsStream(theAssetPath)) == null)
            throw new IOException("Failed to find asset: " + theAssetPath);

        return assetStream;
    }

    @Nullable
    @Override
    public InputStream getAsset(@NonNull String assetPath) {
        try {
            return getAssetInternal(assetPath);
        }
        catch (IOException ignored) {}
        return null;
    }

    @Override
    public void copyAsset(@NonNull Path destination, @NonNull String assetPath) throws UncheckedIOException {
        copyAsset(destination, assetPath, false);
    }

    @Override
    public void copyAsset(@NonNull Path destination, @NonNull String assetPath, boolean overwriteExisting) throws UncheckedIOException {
        try {
            if(Files.exists(destination) && !overwriteExisting)
                return;

            InputStream assetStream = getAssetInternal(assetPath);
            Files.copy(assetStream, destination, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
