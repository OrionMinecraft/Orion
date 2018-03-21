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

package eu.mikroskeem.orion.core.guice;

import com.google.inject.TypeLiteral;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import eu.mikroskeem.shuriken.instrumentation.ClassTools;
import eu.mikroskeem.shuriken.reflect.Reflect;
import net.minecraft.launchwrapper.Launch;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.mikroskeem.orion.core.guice.TypeLiteralGenerator.SignatureBuilder.of;

/**
 * There are some limits with {@link com.google.inject.TypeLiteral}, so let's use ASM to jump over that limit
 *
 * @author Mark Vainomaa
 */
public final class TypeLiteralGenerator {
    private final static AtomicInteger generatedCounter = new AtomicInteger(0);
    private final static Map<SignatureBuilder, TypeLiteral<?>> generated = new HashMap<>();
    private final static Type thisType = Type.getType(TypeLiteralGenerator.class);
    private final static Type typeLiteralType = Type.getType(TypeLiteral.class);

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> get(@NotNull SignatureBuilder signatureBuilder) {
        return (TypeLiteral<T>) generated.computeIfAbsent(signatureBuilder, c -> generate(of(TypeLiteral.class, signatureBuilder).build()));
    }

    @NotNull
    private static TypeLiteral<?> generate(@NotNull String signature) {
        // Figure out new inner class name
        String className = TypeLiteralGenerator.class.getName() + "$g" + generatedCounter.incrementAndGet();

        // Start writing an inner class
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                ClassTools.unqualifyName(className),
                signature,
                typeLiteralType.getInternalName(),
                null);

        cw.visitOuterClass(thisType.getInternalName(), null, null);
        cw.visitInnerClass(ClassTools.unqualifyName(className), null, null, Opcodes.ACC_STATIC);
        ClassTools.generateSimpleSuperConstructor(cw, TypeLiteral.class);
        cw.visitEnd();

        // Load TypeLiteral extending class
        byte[] classData = cw.toByteArray();

        @SuppressWarnings("unchecked")
        Class<? extends TypeLiteral<?>> definedLiteral = (Class<? extends TypeLiteral<?>>)
                Ensure.notNull(ClassLoaderTools.defineClass(Launch.classLoader, className, classData), "Failed to define TypeLiteral class: " + className);

        return Ensure.notNull(Reflect.wrapClass(definedLiteral).construct().getClassInstance(), "Class instance was null!");
    }

    public final static class SignatureBuilder {
        private final String base;
        private final List<SignatureBuilder> parameters;

        private SignatureBuilder(@NotNull String base, @NotNull List<SignatureBuilder> parameters) {
            this.base = base;
            this.parameters = parameters;
        }

        @NotNull
        public String build() {
            StringBuilder builder = new StringBuilder();
            builder.append('L')
                   .append(base);
            if(parameters.size() > 0) {
                builder.append('<');
                for(SignatureBuilder subBuilder : parameters)
                    builder.append(subBuilder.build());
                builder.append('>');
            }
            builder.append(';');
            return builder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof SignatureBuilder)) return false;
            SignatureBuilder that = (SignatureBuilder) o;
            return Objects.equals(base, that.base) &&
                    Objects.equals(parameters, that.parameters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(base, parameters);
        }

        @NotNull
        public static SignatureBuilder of(@NotNull Class<?> base, @NotNull SignatureBuilder... parameters) {
            return of(Type.getType(base), parameters);
        }

        @NotNull
        public static SignatureBuilder of(@NotNull Type base, @NotNull SignatureBuilder... parameters) {
            return new SignatureBuilder(base.getInternalName(), Arrays.asList(parameters));
        }
    }
}
