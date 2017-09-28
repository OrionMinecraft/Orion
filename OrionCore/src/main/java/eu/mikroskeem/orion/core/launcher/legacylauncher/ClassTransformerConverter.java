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

package eu.mikroskeem.orion.core.launcher.legacylauncher;

import eu.mikroskeem.orion.api.bytecode.OrionTransformer;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import net.minecraft.launchwrapper.IClassTransformer;
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
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static eu.mikroskeem.shuriken.common.streams.ByteArrays.fromInputStream;
import static java.util.Objects.requireNonNull;

/**
 * This is so hacky code omg
 *
 * @author Mark Vainomaa
 */
final class ClassTransformerConverter {
    private final static Logger log = LogManager.getLogger("ClassTransformerConverter");

    private final static AtomicInteger COUNTER = new AtomicInteger(0);
    private final static Method OT_TRANSFORM = OrionTransformer.class.getMethods()[0];
    private final static String ICT_INTERNAL = Type.getInternalName(IClassTransformer.class);
    private final static Method ICT_TRANSFORM = IClassTransformer.class.getMethods()[0];

    @NotNull
    @SuppressWarnings("unchecked")
    static <T extends IClassTransformer> Class<T> convert(@NotNull Class<? extends OrionTransformer> transformer) {
        if(Arrays.asList(transformer.getInterfaces()).contains(IClassTransformer.class))
            return (Class<T>) transformer; /* no-op */

        String rawName = transformer.getName().replace('.', '/');

        byte[] rawTransformer = fromInputStream(transformer.getClassLoader().getResourceAsStream(rawName.concat(".class")));
        String newName = getNewName(transformer);
        Type transformerType = Type.getObjectType(newName.replace('.', '/'));

        ClassReader cr = new ClassReader(rawTransformer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        /* Start transforming */
        log.trace("Transforming class {}(s:{}) implementing {} -> {}",
                cn.name.replace('/', '.'),
                cn.superName.replace('/', '.'),
                ((List<String>)cn.interfaces).stream().map(i -> i.replace('/', '.')).collect(Collectors.toList()),
                newName
        );

        cn.interfaces.add(ICT_INTERNAL);
        cn.methods.add(newProxyMethod());

        /*
         * TODO: figure out why this is broken:
         * cn.accept(new ClassRemapper(cw, new org.objectweb.asm.commons.SimpleRemapper(rawName, transformerType.getInternalName())));
         */

        cn.accept(new Remapper(cw, rawName, transformerType.getInternalName()));

        return requireNonNull((Class<T>) ClassLoaderTools.defineClass(ClassLoader.getSystemClassLoader(), newName, cw.toByteArray()));
    }

    private static MethodNode newProxyMethod() {
        MethodNode mn = new MethodNode();
        mn.access = Opcodes.ACC_PUBLIC;
        mn.name = "transform";
        mn.desc = Type.getMethodDescriptor(ICT_TRANSFORM);
        mn.signature = "";
        mn.exceptions = Collections.emptyList();

        /* Ugh */
        mn.visitCode();

        mn.visitVarInsn(Opcodes.ALOAD, 0);

        /* Load arguments */
        mn.visitVarInsn(Opcodes.ALOAD, 3);
        mn.visitVarInsn(Opcodes.ALOAD, 1);
        mn.visitVarInsn(Opcodes.ALOAD, 2);

        /* Invoke self */
        mn.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                OT_TRANSFORM.getDeclaringClass().getName().replace('.', '/'),
                "transformClass",
                Type.getMethodDescriptor(OT_TRANSFORM),
                true
        );

        /* Return */
        mn.visitInsn(Opcodes.ARETURN);
        mn.visitMaxs(0, 0);
        mn.visitEnd();

        return mn;
    }

    private static String getNewName(Class<?> clazz) {
        String name = clazz.getName();
        return ClassTransformerConverter.class.getName()
                .concat("$")
                .concat("" + COUNTER.getAndIncrement())
                .concat("$")
                .concat(name.substring(name.lastIndexOf('.') + 1));
    }

    static class Remapper extends ClassVisitor {
        private final String oldName;
        private final String newName;

        Remapper(ClassVisitor cv, String oldName, String newName) {
            super(Opcodes.ASM5, cv);
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if(oldName.equals(name)) name = newName;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodRemapper(this, super.visitMethod(access, name, desc, signature, exceptions));
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if(oldName.equals(name)) name = newName;
            return super.visitField(access, name, desc, signature, value);
        }
    }

    static class MethodRemapper extends MethodVisitor {
        private final Remapper sr;

        MethodRemapper(Remapper sr, MethodVisitor mv) {
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
}
