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

import eu.mikroskeem.orion.api.mod.ModInfo;
import eu.mikroskeem.orion.core.launcher.BlackboardKey;
import eu.mikroskeem.orion.core.launcher.LauncherService;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Objects;


/**
 * Mod info container class implementing {@link ModInfo}
 *
 * @author Mark Vainomaa
 */
final class OrionModInfo implements ModInfo {
    private String id;
    private String className;
    private List<String> dependencies;
    private String configClassName;
    private Class<?> configClass = null;

    @NonNull
    public String getId() {
        return id;
    }

    void setId(@NonNull String id) {
        this.id = Objects.requireNonNull(id, "Id cannot be null!");
    }

    @NonNull
    public String getClassName() {
        return className;
    }

    void setClassName(@NonNull String className) {
        this.className = Objects.requireNonNull(className, "Class name cannot be null!");
    }

    @NonNull
    public List<@NonNull String> getDependencies() {
        return dependencies;
    }

    void setDependencies(@NonNull List<@NonNull String> dependencies) {
        this.dependencies = Objects.requireNonNull(dependencies, "Dependencies cannot be null!");
    }

    @NonNull
    @Override
    public Class<?> getConfigClass() {
        if(configClass == null) {
            ClassLoader loader = BlackboardKey.<LauncherService>get(BlackboardKey.LAUNCHER_SERVICE).getClassLoader();
            try {
                configClass = Class.forName(configClassName, true, loader);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Failed to find specified configuration class!", e);
            }
        }
        return configClass;
    }

    void setConfigClass(@NonNull String configClass) {
        this.configClassName = Objects.requireNonNull(configClass, "Configuration class name cannot be null!");
    }

    @Override
    public String toString() {
        return "ModInfo{" +
                "id='" + id + '\'' +
                ", className='" + className + '\'' +
                ", dependencies=" + dependencies +
                ", configClass=" + configClass +
                '}';
    }
}
