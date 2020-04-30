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

import eu.mikroskeem.orion.api.bytecode.OrionTransformer;
import eu.mikroskeem.orion.at.AccessTransformer;
import eu.mikroskeem.orion.core.launcher.BlackboardKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Orion Access Transformer library wrapper
 *
 * @author Mark Vainomaa
 */
public final class OrionAccessTransformer implements OrionTransformer {
    private static final Logger logger = LogManager.getLogger("OrionAT");
    private final AccessTransformer at = new AccessTransformer();

    public OrionAccessTransformer() {
        List<URL> atUrls = BlackboardKey.getOr(BlackboardKey.AT_URLS, ArrayList::new);
        logger.debug("Initializing OrionAT with {} access transformers", atUrls.size());

        /* Load AT files to AT library */
        for(URL url : atUrls) {
            try {
                URLConnection urlConnection = url.openConnection();
                urlConnection.setUseCaches(false);
                logger.debug("Processing AT {}", url);
                at.loadAccessTransformers(urlConnection.getInputStream());
            } catch (IOException e) {
                logger.warn("Skipping AT {}", e);
            }
        }

        logger.debug("OrionAT initialized");
    }

    /**
     * Register access transformer from jar resource
     *
     * @param url AT url from {@link Class#getResource(String)}
     */
    public static void registerAT(@NotNull URL url) {
        try {
            /* Test connection */
            URLConnection urlConnection = url.openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.connect();
            BlackboardKey.<List<URL>>getOr(BlackboardKey.AT_URLS, ArrayList::new).add(url);
            logger.debug("Registered AT {}", url);
        } catch (IOException e) {
            logger.warn("Failed to register AT {}", url);
        }
    }

    @Nullable
    @Override
    public byte[] transformClass(@Nullable byte[] source, @NotNull String className, @NotNull String remappedClassName) {
        if(source == null)
            return null;

        logger.debug("Transforming class {}", className);
        return at.transformClass(source);
    }
}
