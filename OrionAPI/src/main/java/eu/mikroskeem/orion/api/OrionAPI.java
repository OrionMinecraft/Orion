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

package eu.mikroskeem.orion.api;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;


/**
 * {@link Orion} implementation container for static access
 *
 * @author Mark Vainomaa
 */
public final class OrionAPI {
    /** {@link eu.mikroskeem.orion.api.Orion} instance */
    private static Orion instance;

    /**
     * Gets {@link Orion} instance
     *
     * @return {@link Orion} instance
     */
    @NonNull
    public static Orion getInstance() {
        return instance;
    }

    /**
     * Sets {@link Orion} instance. Can be done only once, as this class is singleton container
     *
     * @param instance {@link Orion}
     */
    public static void setInstance(@NonNull Orion instance) {
        Objects.requireNonNull(instance, "Can not set Orion instance to null!");
        if(OrionAPI.instance != null) throw new IllegalStateException("Orion instance is already set!");
        OrionAPI.instance = instance;
    }
}
