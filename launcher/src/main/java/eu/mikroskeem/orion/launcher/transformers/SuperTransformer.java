package eu.mikroskeem.orion.launcher.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

/**
 * @author Mark Vainomaa
 */
public class SuperTransformer implements IClassTransformer {
    private final static String SHURIKEN =  "eu/mikroskeem/shuriken/classloader/ShurikenClassLoader";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!transformedName.equals("org.bukkit.plugin.java.PluginClassLoader")) {
            return basicClass;
        }
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, 0);
        SuperTransformerImpl st = new SuperTransformerImpl(cw);
        cr.accept(st, 0);
        return cw.toByteArray();
    }

    private static class SuperTransformerImpl extends ClassVisitor {
        private SuperTransformerImpl(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            cv.visit(Opcodes.V1_8, access, name, signature, SHURIKEN, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new SuperTransformerMethodVisitorImpl(Opcodes.ASM5,
                    cv.visitMethod(access, name, desc, signature, exceptions));
        }
    }

    private static class SuperTransformerMethodVisitorImpl extends MethodVisitor {
        public SuperTransformerMethodVisitorImpl(int api, MethodVisitor mv) { super(api, mv); }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if(owner.equals("java/net/URLClassLoader")) owner = SHURIKEN;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
