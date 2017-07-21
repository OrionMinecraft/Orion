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

package eu.mikroskeem.orion.core.lcl;

import eu.mikroskeem.shuriken.instrumentation.methodreflector.MethodReflector;
import eu.mikroskeem.shuriken.instrumentation.methodreflector.TargetFieldGetter;
import eu.mikroskeem.shuriken.reflect.Reflect;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.util.Set;
import java.util.WeakHashMap;


/**
 * @author Mark Vainomaa
 */
public interface LaunchClassLoaderAccessor {
    @TargetFieldGetter("classLoaderExceptions") Set<String> getClassLoaderExceptions();

    class WrappedInstances {
        private final static WeakHashMap<LaunchClassLoader, LaunchClassLoaderAccessor> instances = new WeakHashMap<>();

        public static LaunchClassLoaderAccessor getAccessor(LaunchClassLoader launchClassLoader) {
            return instances.computeIfAbsent(launchClassLoader, lcl ->
                MethodReflector.newInstance(Reflect.wrapInstance(lcl), LaunchClassLoaderAccessor.class).getReflector()
            );
        }
    }
}
