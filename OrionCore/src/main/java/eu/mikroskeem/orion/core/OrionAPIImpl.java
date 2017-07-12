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

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.picomaven.Dependency;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Orion API implementation
 *
 * @author Mark Vainomaa
 */
final class OrionAPIImpl implements Orion {
    private static final Logger logger = LogManager.getLogger("OrionAPI");
    private final OrionCore orionCore;

    OrionAPIImpl(OrionCore orionCore) {
        this.orionCore = orionCore;
    }

    @Override
    public void registerAT(URL atUrl) {
        OrionAccessTransformer.registerAT(atUrl);
    }

    @Override
    public void registerMixinConfig(String mixinConfigName) {
        /* TODO: Enforce mixin name formatting */

        /* Check if mixin configuration with same name already exists, as people like to do dumb stuff... */
        Ensure.ensureCondition(!orionCore.mixinConfigurations.contains(mixinConfigName),
                "Mixin configuration with name '" + mixinConfigName + "' already exists!");

        orionCore.mixinConfigurations.add(mixinConfigName);
        Mixins.addConfiguration(mixinConfigName);
    }

    @Override
    public List<URL> getRegisteredMavenRepositories() {
        return Collections.unmodifiableList(orionCore.modMavenRepositories.stream()
                .map(u -> {
                    try {
                        return u.toURL();
                    } catch (MalformedURLException e) {
                        SneakyThrow.throwException(e);
                        return null;
                    }
                })
                .collect(Collectors.toList())
        );
    }

    @Override
    public void registerMavenRepository(URL url) {
        try {
            orionCore.modMavenRepositories.add(url.toURI());
        } catch (URISyntaxException e) {
            SneakyThrow.throwException(e);
        }
    }

    @Override
    public void registerLibrary(String dependencyString) {
        orionCore.modLibraries.add(Dependency.fromGradle(dependencyString));
    }

    @Override
    public List<String> getRegisteredLibraries() {
        return Collections.unmodifiableList(orionCore.modLibraries.stream()
                .map(d -> d.getGroupId() + ':' + d.getArtifactId() + ':' + d.getVersion())
                .collect(Collectors.toList()));
    }
}
