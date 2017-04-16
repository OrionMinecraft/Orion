package eu.mikroskeem.orion.mod.transformers;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * @author Mark Vainomaa
 */
public class AccessLevelTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!transformedName.equals("org.bukkit.plugin.java.PluginClassLoader")) {
            return basicClass;
        }
        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, 0);
        AccessLevelTransformerImpl at = new AccessLevelTransformerImpl(cw);
        cr.accept(at, 0);
        return cw.toByteArray();
    }

    private static class AccessLevelTransformerImpl extends ClassVisitor {
        private AccessLevelTransformerImpl(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            int newAccess = Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER;
            cv.visit(Opcodes.V1_8, newAccess, name, signature, superName, interfaces);
        }
    }
}
