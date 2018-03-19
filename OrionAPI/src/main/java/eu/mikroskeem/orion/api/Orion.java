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

package eu.mikroskeem.orion.api;

import eu.mikroskeem.orion.api.asset.AssetManager;
import eu.mikroskeem.orion.api.bytecode.OrionTransformer;
import eu.mikroskeem.orion.api.mod.ModInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.Set;


/**
 * Orion API for mods
 *
 * @author Mark Vainomaa
 */
public interface Orion {
    /**
     * Registers access transformer to be applied on server start.
     *
     * @param atUrl Resource {@link URL} of access transformer file (in other words {@link Class#getResource(String)})
     */
    void registerAT(@NotNull URL atUrl);

    /**
     * Registers mixin config.
     *
     * Note: Mixin configuration name format is checked on register.
     *
     * @param mixinConfigName Mixin configuration name in format: <pre>mixins.MOD_ID.WHATEVER.json</pre>
     */
    void registerMixinConfig(@NotNull String mixinConfigName);

    /**
     * Gets list of registered Maven repositories
     *
     * @return {@link List} of Maven repository {@link URL}s
     */
    @NotNull
    List<URL> getRegisteredMavenRepositories();

    /**
     * Registers Maven repository where libraries can be downloaded
     *
     * @param url Maven repository url
     * @see Orion#registerLibrary(String)
     */
    void registerMavenRepository(@NotNull URL url);

    /**
     * Gets list of registered mod libraries
     *
     * @return {@link List} of mod libraries
     * @see Orion#registerLibrary(String)
     */
    @NotNull
    List<String> getRegisteredLibraries();

    /**
     * Add new library for downloading from Maven repository.
     *
     * @param dependencyString Dependency string, like <pre>groupId:artifactId:version</pre>
     */
    void registerLibrary(@NotNull String dependencyString);

    /**
     * Gets unmodifiable list of loaded mods
     *
     * @return Unmodifiable list of loaded mods
     */
    @NotNull
    List<ModInfo> getMods();

    /**
     * Gets mod by its id
     *
     * @since 0.0.4-SNAPSHOT
     * @param modId Mod id
     * @return Instance of {@link ModInfo} or null, if mod wasn't found
     */
    @Nullable
    ModInfo getMod(@NotNull String modId);

    /**
     * Gets unmodifiable list of registered Mixin configurations
     *
     * @return Unmodifiable list of registered Mixin configurations
     */
    @NotNull
    List<String> getMixinConfigurations();

    /**
     * Register new {@link OrionTransformer} class
     *
     * @since 0.0.3-SNAPSHOT
     * @param transformer New transformer
     */
    void registerTransformer(@NotNull Class<? extends OrionTransformer> transformer);

    /**
     * Unregisters registered {@link OrionTransformer} class
     *
     * @since 0.0.3-SNAPSHOT
     * @param transformer Registered transformer. Check if transformer is registered with {@link #getRegisteredTransformers()}
     */
    void unregisterTransformer(@NotNull Class<? extends OrionTransformer> transformer);

    /**
     * Gets list of registered Orion bytecode transformers
     *
     * @since 0.0.3-SNAPSHOT
     * @return {@link List} of registered {@link OrionTransformer}s
     */
    @NotNull
    Set<Class<? extends OrionTransformer>> getRegisteredTransformers();

    /**
     * Gets mod asset manager
     *
     * @since 0.0.4-SNAPSHOT
     * @return Mod asset manager
     */
    @NotNull
    AssetManager.ForMod getAssetManager();
}
