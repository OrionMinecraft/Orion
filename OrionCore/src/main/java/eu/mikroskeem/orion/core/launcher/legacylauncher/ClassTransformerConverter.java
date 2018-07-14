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

package eu.mikroskeem.orion.core.launcher.legacylauncher;

import eu.mikroskeem.orion.api.bytecode.OrionTransformer;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.mikroskeem.shuriken.common.streams.ByteArrays.fromInputStream;
import static java.util.Objects.requireNonNull;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Wraps Orion transformer into LegacyLauncher-compatible transformer
 *
 * @author Mark Vainomaa
 */
final class ClassTransformerConverter {
    private final static Logger log = LogManager.getLogger("ClassTransformerConverter");

    private final static AtomicInteger COUNTER = new AtomicInteger(0);
    private final static byte[] TW_DATA;
    private final static Type TW_TYPE = Type.getType(TransformerWrapper.class);
    private final static Type DT_TYPE = Type.getType(DummyTransformer.class);

    @NotNull
    @SuppressWarnings("unchecked")
    static <T extends IClassTransformer> Class<T> convert(@NotNull Class<? extends OrionTransformer> transformer) {
        if(IClassTransformer.class.isAssignableFrom(transformer))
            return (Class<T>) transformer; /* no-op */

        // Orion transformer name
        String rawTransformerName = transformer.getName().replace('.', '/');

        // New wrapper class name
        String newWrapperName = getNewName(transformer);

        // Debug log
        log.debug("Wrapping transformer class '{}' to '{}'", transformer.getName(), newWrapperName);

        // Start generating new TransformerWrapper
        ClassReader cr = new ClassReader(TW_DATA);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        cn.accept(new WrapperGenerator(cw, TW_TYPE.getInternalName(), newWrapperName.replace('.', '/'), rawTransformerName));

        return requireNonNull((Class<T>) ClassLoaderTools.defineClass(Launch.classLoader, newWrapperName, cw.toByteArray()));
    }

    private static String getNewName(@NotNull Class<?> clazz) {
        String name = clazz.getName();
        return ClassTransformerConverter.class.getName()
                .concat("$")
                .concat("" + COUNTER.getAndIncrement())
                .concat("$")
                .concat(name.substring(name.lastIndexOf('.') + 1));
    }

    static class WrapperGenerator extends ClassVisitor {
        private final String oldName;
        private final String newName;
        private final String transformerName;

        WrapperGenerator(ClassVisitor cv, String oldName, String newName, String transformerName) {
            super(Opcodes.ASM5, cv);
            this.oldName = oldName;
            this.newName = newName;
            this.transformerName = transformerName;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if(oldName.equals(name)) name = newName;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodRemapper mr = new MethodRemapper(this, super.visitMethod(access, name, desc, signature, exceptions));
            if("<init>".equals(name) && desc.equals("()V")) {
                return new InitTransformer(this, mr, transformerName);
            }
            return mr;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if(oldName.equals(name)) name = newName;
            return super.visitField(access, name, desc, signature, value);
        }
    }

    static class MethodRemapper extends MethodVisitor {
        private final WrapperGenerator sr;

        MethodRemapper(WrapperGenerator sr, MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
            this.sr = sr;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if(sr.oldName.equals(owner)) owner = sr.newName;
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if(sr.oldName.equals(owner)) owner = sr.newName;
            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    static class InitTransformer extends MethodVisitor {
        private final WrapperGenerator sr;
        private final String transformerName;

        InitTransformer(WrapperGenerator sr, MethodVisitor mv, String transformerName) {
            super(Opcodes.ASM5, mv);
            this.sr = sr;
            this.transformerName = transformerName;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if(opcode == INVOKESPECIAL && owner.equals(DT_TYPE.getInternalName())) {
                owner = transformerName;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if(opcode == NEW && DT_TYPE.getInternalName().equals(type)) {
                type = transformerName;
            }
            super.visitTypeInsn(opcode, type);
        }
    }

    static {
         TW_DATA = fromInputStream(TransformerWrapper.class.getResourceAsStream('/' +
                 TransformerWrapper.class.getName().replace('.', '/').concat(".class")
         ));
    }
}
