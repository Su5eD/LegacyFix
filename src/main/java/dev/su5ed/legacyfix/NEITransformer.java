package dev.su5ed.legacyfix;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings({"unused", "unchecked"})
public class NEITransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, byte[] bytes) {
        if (name.equals("codechicken.nei.TMIUninstaller")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);

            patchTMIUninstaller(classNode);

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);

            return writer.toByteArray();
        }
        return bytes;
    }

    private static void patchTMIUninstaller(ClassNode classNode) {
        for (MethodNode m : (List<MethodNode>) classNode.methods) {
            if (m.name.equals("getJarFile") && m.desc.equals("()Ljava/io/File;")) {
                InsnList list = new InsnList();
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/su5ed/legacyfix/LegacyFixTransformer", "getJarFile", "()Ljava/io/File;"));
                list.add(new InsnNode(Opcodes.ARETURN));
                m.instructions.insert(list);
            }
        }
    }

    public static File getJarFile() {
        ModClassLoader classLoader = (ModClassLoader) Loader.instance().getModClassLoader();
        for (File file : classLoader.getParentSources()) {
            try (ZipFile archive = new ZipFile(file)) {
                ZipEntry serverClass = archive.getEntry("net/minecraft/server/MinecraftServer.class");
                if (serverClass != null) {
                    return file;
                }
            } catch (IOException ignored) {}
        }
        return null;
    }
}
