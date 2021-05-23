package mods.su5ed.legacyfix;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.asm.transformers.TransformerCompatLayer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LegacyFixTransformer implements IClassTransformer {
	private static final Map<String, Consumer<ClassNode>> transformers = new HashMap<>();
	
	static {
		transformers.put("miscperipherals.asm.BlockTurtleTransformer", LegacyFixTransformer::patchAsmApiNumber);
		transformers.put("miscperipherals.asm.TileEntityTurtleTransformer", LegacyFixTransformer::patchAsmApiNumber);
		transformers.put("codechicken.nei.TMIUninstaller", LegacyFixTransformer::patchTMIUninstaller);
		transformers.put("immibis.core.ImmibisCore", LegacyFixTransformer::patchImmibisCore);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if (transformers.containsKey(name)) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);
			        
			transformers.get(name).accept(classNode);
					
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			        
			return writer.toByteArray();
		}
		return bytes;
	}
	
	private static void patchAsmApiNumber(ClassNode classNode) {
		for (MethodNode m : classNode.methods) {
			Iterator<AbstractInsnNode> it = m.instructions.iterator();
			for (int i = 0; it.hasNext(); i++) {
				AbstractInsnNode insn = it.next();
						
				if (insn instanceof TypeInsnNode) {
					TypeInsnNode node = (TypeInsnNode) insn;
					if (node.getOpcode() == Opcodes.NEW && node.desc.equals("org/objectweb/asm/tree/MethodNode")) {
						AbstractInsnNode insnNode = m.instructions.get(i + 2);
						
						if (insnNode.getOpcode() == Opcodes.ICONST_4) {
							m.instructions.set(insnNode, new LdcInsnNode(Opcodes.ASM4));
						}
					}
				}
			}
		}
	}
	
	private static void patchTMIUninstaller(ClassNode classNode) {
		for (MethodNode m : classNode.methods) {
			if (m.name.equals("getJarFile") && m.desc.equals("()Ljava/io/File;")) {
				InsnList list = new InsnList();
				list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/su5ed/legacyfix/LegacyFixTransformer", "getJarFile", "()Ljava/io/File;", false));
				list.add(new InsnNode(Opcodes.ARETURN));
				m.instructions.insertBefore(m.instructions.getFirst(), list);
			}
		}
	}
	
	public static File getJarFile() {
		for(File file : ((ModClassLoader) Loader.instance().getModClassLoader()).getParentSources()) {
			try(ZipFile archive = new ZipFile(file)) {
				ZipEntry serverClass = archive.getEntry("net/minecraft/server/MinecraftServer.class");
				if (serverClass != null) return file;
			}
			catch (IOException ignored) {}
		}
		return null;
	}
	
	private static void patchImmibisCore(ClassNode classNode) {
		for (MethodNode m : classNode.methods) {
			if (m.name.equals("<clinit>") && m.desc.equals("()V")) {
				for (Iterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); ) {
					AbstractInsnNode insn = it.next();
					
					if (insn instanceof MethodInsnNode) {
						MethodInsnNode methodInsn = (MethodInsnNode) insn;
						if (methodInsn.owner.equals("net/minecraft/launchwrapper/LaunchClassLoader") && methodInsn.name.equals("registerTransformer") && methodInsn.desc.equals("(Ljava/lang/String;)V")) {
							methodInsn.setOpcode(Opcodes.INVOKESTATIC);
							methodInsn.owner = "mods/su5ed/legacyfix/LegacyFixTransformer";
						}
					}
				}
			}
		}
	}
	
	public static void registerTransformer(String name) {
		try {
			TransformerCompatLayer.registerTransformer((cpw.mods.fml.relauncher.IClassTransformer) Class.forName(name).newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}