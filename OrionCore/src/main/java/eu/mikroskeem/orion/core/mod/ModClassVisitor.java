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

import eu.mikroskeem.orion.api.annotations.OrionMod;
import eu.mikroskeem.orion.api.configuration.DummyConfiguration;
import eu.mikroskeem.orion.api.mod.ModInfo;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Orion mod class visitor. Reads {@link OrionMod} annotation directly from bytecode.
 *
 * Based off from a code in Sponge mod loader
 *
 * @author Mark Vainomaa
 */
public final class ModClassVisitor extends ClassVisitor {
    private final static Type ORIONMOD_ANNOTATION = Type.getType(OrionMod.class);

    private ModClassVisitor() {
        super(Opcodes.ASM5);
    }

    /** Mod annotation visitor instance */
    @Nullable private ModAnnotationVisitor modAnnotationVisitor;

    /** Visitable class name */
    @Nullable private String className;

    /**
     * Gets mod info from class bytes
     *
     * @param classData Class bytes
     * @return Instance of {@link ModInfo}, or null if not present
     */
    @Nullable
    public static ModInfo getModInfo(byte[] classData) {
        ClassReader classReader = new ClassReader(classData);
        ModClassVisitor modClassVisitor = new ModClassVisitor();
        classReader.accept(modClassVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return modClassVisitor.modAnnotationVisitor != null &&  modClassVisitor.modAnnotationVisitor.gotId ?
                modClassVisitor.modAnnotationVisitor.modInfo : null;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return (visible && desc.equals(ORIONMOD_ANNOTATION.getDescriptor())) ?
                modAnnotationVisitor = new ModAnnotationVisitor(className) : super.visitAnnotation(desc, visible);
    }

    static class ModAnnotationVisitor extends AnnotationVisitor {
        private final List<String> dependencies = new ArrayList<>();
        private State currentState = State.DEFAULT;
        private OrionModInfo modInfo;
        private boolean gotId = false;

        private enum State {
            DEFAULT, DEPENDENCIES
        }

        ModAnnotationVisitor(String className) {
            super(Opcodes.ASM5);
            modInfo = new OrionModInfo();
            modInfo.setDependencies(dependencies);
            modInfo.setConfigClass(DummyConfiguration.class.getName());
            modInfo.setClassName(className.replace('/', '.'));
        }

        private void check(State state) {
            if(currentState != state)
                    throw new IllegalStateException("Expected state " + state + ", but is " + currentState);
        }

        @Override
        public void visit(String name, Object value) {
            if(currentState == State.DEPENDENCIES) {
                dependencies.add((String)value);
                return;
            }

            check(State.DEFAULT);
            Objects.requireNonNull(name, "Name is null");
            switch (name) {
                case "id":
                    gotId = true;
                    modInfo.setId((String)value);
                    break;
                case "configurationClass":
                    Type configClass = (Type) value;
                    modInfo.setConfigClass(configClass.getClassName());
                    break;
                default:
                    super.visit(name, value);
            }
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            Objects.requireNonNull(name, "Name is null");
            switch (name) {
                case "dependencies":
                    this.currentState = State.DEPENDENCIES;
                    return this;
                default:
                    return super.visitArray(name);
            }
        }

        @Override
        public void visitEnd() {
            if(this.currentState != State.DEFAULT) {
                this.currentState = State.DEFAULT;
                return;
            }

            if(!gotId)
                throw new IllegalStateException("Mod annotation doesn't have required element 'id'");
        }
    }
}
