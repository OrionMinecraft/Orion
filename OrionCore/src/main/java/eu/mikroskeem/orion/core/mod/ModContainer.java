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

import com.google.common.eventbus.EventBus;
import eu.mikroskeem.orion.api.events.ModConstructEvent;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.injector.Injector;
import eu.mikroskeem.shuriken.reflect.ClassWrapper;
import org.jetbrains.annotations.Contract;


/**
 * Orion Mod container
 *
 * @author Mark Vainomaa
 */
public final class ModContainer<T> {
    private final ClassWrapper<T> modClass;
    private final ModInfo modInfo;
    private final Injector injector;
    private final EventBus eventBus;

    public ModContainer(ClassWrapper<T> modClass, ModInfo modInfo, Injector injector, EventBus eventBus) {
        this.modClass = modClass;
        this.modInfo = modInfo;
        this.injector = injector;
        this.eventBus = eventBus;
    }

    /**
     * Initializes mod
     */
    public void init() {
        Ensure.ensureCondition(modClass.getClassInstance() == null, "Mod is already initialized!");
        Object instance = injector.getInstance(modClass.getWrappedClass());
        modClass.setClassInstance(instance);
        eventBus.register(instance);
        eventBus.post(new ModConstructEvent());
    }

    /**
     * Gets mod info
     *
     * @return instance of {@link ModInfo}
     */
    @Contract(pure = true)
    public ModInfo getModInfo() {
        return modInfo;
    }

    /**
     * Gets mod event bus
     *
     * @return {@link EventBus} of mod
     */
    @Contract(pure = true)
    public EventBus getEventBus() {
        return eventBus;
    }
}