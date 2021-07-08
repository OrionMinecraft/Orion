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

package eu.mikroskeem.orion.core;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.asset.AssetManager;
import eu.mikroskeem.orion.api.bytecode.OrionTransformer;
import eu.mikroskeem.orion.api.mod.ModInfo;
import eu.mikroskeem.orion.core.mod.AssetManagerImpl;
import eu.mikroskeem.orion.core.mod.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Orion API implementation
 *
 * @author Mark Vainomaa
 */
final class OrionAPIImpl implements Orion {
    private static final Logger logger = LogManager.getLogger("OrionAPI");
    private static final Pattern MIXIN_NAME_PATTERN = Pattern.compile("mixins\\.(.*\\.)?.*\\.json");
    private final OrionCore orionCore;
    final AssetManagerImpl assetManager;

    private SoftReference<List<URL>> registeredMavenRepositories;
    private SoftReference<List<String>> registeredLibraries;
    private SoftReference<List<ModInfo>> mods;

    OrionAPIImpl(OrionCore orionCore) {
        this.orionCore = orionCore;
        this.assetManager = new AssetManagerImpl();
    }

    @Override
    public void registerMixinConfig(String mixinConfigName) {
        if(!MIXIN_NAME_PATTERN.matcher(mixinConfigName).matches())
                throw new IllegalStateException("Mixin configuration name '" + mixinConfigName + "' does not match pattern '" + MIXIN_NAME_PATTERN + "'");

        /* Check if mixin configuration with same name already exists, as people like to do dumb stuff... */
        if(orionCore.mixinConfigurations.contains(mixinConfigName))
                throw new IllegalStateException("Mixin configuration with name '" + mixinConfigName + "' already exists!");

        orionCore.mixinConfigurations.add(mixinConfigName);
        Mixins.addConfiguration(mixinConfigName);
    }
    

    @Override
    @NonNull
    public List<ModInfo> getMods() {
        if(mods == null || mods.get() == null) {
            mods = new SoftReference<>(orionCore.mods.stream()
                            .map(ModContainer::getModInfo)
                            .collect(Collectors.toList())
            );
        }

        return Objects.requireNonNull(mods.get());
    }

    @Nullable
    @Override
    public ModInfo getMod(@NonNull String modId) {
        return getMods().stream().filter(m -> m.getId().equals(modId)).findFirst().orElse(null);
    }

    @Override
    @NonNull
    public List<String> getMixinConfigurations() {
        return Collections.unmodifiableList(orionCore.mixinConfigurations);
    }

    @Override
    public void registerTransformer(@NonNull Class<? extends OrionTransformer> transformer) {
        orionCore.transformers.add(transformer);
    }

    @Override
    public void unregisterTransformer(@NonNull Class<? extends OrionTransformer> transformer) {
        orionCore.transformers.remove(transformer);
    }

    @Override
    @NonNull
    public Set<Class<? extends OrionTransformer>> getRegisteredTransformers() {
        return Collections.unmodifiableSet(orionCore.transformers);
    }

    @Override
    public AssetManager.@NonNull ForMod getAssetManager() {
        return assetManager;
    }
}
