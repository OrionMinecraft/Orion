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

package eu.mikroskeem.orion.core.mod;

import eu.mikroskeem.orion.api.mod.ModInfo;
import eu.mikroskeem.shuriken.common.Ensure;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * Mod info container class implementing {@link ModInfo}
 *
 * @author Mark Vainomaa
 */
final class OrionModInfo implements ModInfo {
    private String id;
    private String className;
    private List<String> dependencies;

    @NotNull
    @Contract(pure = true)
    public String getId() {
        return id;
    }

    @Contract("null -> fail")
    void setId(@NotNull String id) {
        this.id = Ensure.notNull(id, "Id cannot be null!");
    }

    @NotNull
    @Contract(pure = true)
    public String getClassName() {
        return className;
    }

    @Contract("null -> fail")
    void setClassName(@NotNull String className) {
        this.className = Ensure.notNull(className, "Class name cannot be null!");
    }

    @NotNull
    @Contract(pure = true)
    public List<String> getDependencies() {
        return dependencies;
    }

    @Contract("null -> fail")
    void setDependencies(@NotNull List<String> dependencies) {
        this.dependencies = Ensure.notNull(dependencies, "Dependencies cannot be null!");
    }

    @Override
    public String toString() {
        return "ModInfo{" + "id='" + id + '\'' + ", className='" + className + '\'' + ", dependencies=" + dependencies + '}';
    }
}
