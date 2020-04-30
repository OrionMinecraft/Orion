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

import eu.mikroskeem.orion.api.OrionAPI;
import eu.mikroskeem.orion.api.asset.AssetManager;
import eu.mikroskeem.orion.api.mod.ModInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vainomaa
 */
public final class AssetManagerImpl implements AssetManager.ForMod {
    private final Map<String, ModAssetManager> assetManagers = new HashMap<>();

    @NonNull
    @Override
    public AssetManager forMod(@NonNull ModInfo modInfo) {
        return assetManagers.computeIfAbsent(modInfo.getId(), ModAssetManager::new);
    }

    @Override
    @Nullable
    public AssetManager forMod(@NonNull String modId) {
        ModInfo modInfo = OrionAPI.getInstance().getMod(modId);
        return modInfo != null ? forMod(modInfo) : null;
    }

    public AssetManager createExplicitly(@NonNull String modId) {
        ModAssetManager assetManager = new ModAssetManager(modId);
        assetManagers.put(modId, assetManager);
        return assetManager;
    }
}
