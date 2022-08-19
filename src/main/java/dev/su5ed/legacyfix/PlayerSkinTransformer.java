package dev.su5ed.legacyfix;

import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unchecked", "unused"})
public class PlayerSkinTransformer implements IClassTransformer {
    public static final String PLAYER_SP = "net.minecraft.client.entity.EntityPlayerSP";
    public static final String PLAYER_SP_OBF = "bag";
    
    public static final String OTHER_PLAYER_MP = "net.minecraft.client.entity.EntityOtherPlayerMP";
    public static final String OTHER_PLAYER_MP_OBF = "ays";
    
    public static final Set<String> TRANSFORMED_CLASSES = Sets.newHashSet(PLAYER_SP, PLAYER_SP_OBF, OTHER_PLAYER_MP, OTHER_PLAYER_MP_OBF);

    @Override
    public byte[] transform(String name, byte[] bytes) {
        if (TRANSFORMED_CLASSES.contains(name)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);

            if (name.equals(PLAYER_SP) || name.equals(OTHER_PLAYER_MP)) {
                patchEntityClientPlayer(name, classNode, false);
            }
            else if (name.equals(PLAYER_SP_OBF) || name.equals(OTHER_PLAYER_MP_OBF)) {
                patchEntityClientPlayer(name, classNode, true);
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            return writer.toByteArray();
        }
        return bytes;
    }
    
    private static void patchEntityClientPlayer(String name, ClassNode classNode, boolean obf) {
        String skinUrlField = obf ? "cu" : "skinUrl";
        String sessionClass = obf ? "ata" : "net/minecraft/util/Session";
        String usernameField = obf ? "b" : "username";
        String updateCloakMethod = obf ? "ad" : "updateCloak";
        String stringUtilsClass = obf ? "km" : "net/minecraft/util/StringUtils";
        String stripControlCodesMethod = obf ? "a" : "stripControlCodes";
        
        for (MethodNode node : (List<MethodNode>) classNode.methods) {
            if (node.name.equals("<init>")) {
                int injectBegin = -1;
                boolean flag = false;
                
                Iterator<AbstractInsnNode> it = node.instructions.iterator();
                for (int i = 0; it.hasNext(); i++) {
                    AbstractInsnNode insn = it.next();
                    if (insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.NEW && ((TypeInsnNode) insn).desc.equals("java/lang/StringBuilder")) {
                        injectBegin = i;
                    }
                    else if (injectBegin != -1 && insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst.equals("http://skins.minecraft.net/MinecraftSkins/")) {
                        flag = true;
                    }
                    else if (flag && insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.PUTFIELD
                        && ((FieldInsnNode) insn).owner.equals(classNode.name) && ((FieldInsnNode) insn).name.equals(skinUrlField)
                    ) {
                        Label skip = new Label();
                        AbstractInsnNode begin = node.instructions.get(injectBegin);
                        InsnList skipList = new InsnList();
                        skipList.add(new JumpInsnNode(Opcodes.GOTO, new LabelNode(skip)));
                        node.instructions.insertBefore(begin, skipList);

                        InsnList list = new InsnList();
                        list.add(new LabelNode(skip));
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "dev/su5ed/legacyfix/skins/PlayerTextures", "INSTANCE", "Ldev/su5ed/legacyfix/skins/PlayerTextures;"));
                        
                        if (name.equals(PLAYER_SP) || name.equals(PLAYER_SP_OBF)) {
                            list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // session
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, sessionClass, usernameField, "Ljava/lang/String;"));
                        } else {
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // username
                        }
                        
                        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, stringUtilsClass, stripControlCodesMethod, "(Ljava/lang/String;)Ljava/lang/String;"));
                        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "dev/su5ed/legacyfix/skins/TextureType", "SKIN", "Ldev/su5ed/legacyfix/skins/TextureType;"));
                        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "dev/su5ed/legacyfix/skins/PlayerTextures", "getTextureURL", "(Ljava/lang/String;Ldev/su5ed/legacyfix/skins/TextureType;)Ljava/lang/String;"));
                        node.instructions.insertBefore(insn, list);
                        
                        break;
                    }
                }
            }
            else if (node.name.equals(updateCloakMethod)) {
                patchUpdateCloak(classNode, node, obf);
            }
        }
    }
    
    private static void patchUpdateCloak(ClassNode classNode, MethodNode methodNode, boolean obf) {
        String playerCloakUrlField = obf ? "cz" : "playerCloakUrl";
        String usernameField = obf ? "bR" : "username";
        String stringUtilsClass = obf ? "km" : "net/minecraft/util/StringUtils";
        String stripControlCodesMethod = obf ? "a" : "stripControlCodes";
        int injectBegin = -1;

        Iterator<AbstractInsnNode> it = methodNode.instructions.iterator();
        for (int i = 0; it.hasNext(); i++) {
            AbstractInsnNode insn = it.next();

            if (insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.NEW && ((TypeInsnNode) insn).desc.equals("java/lang/StringBuilder")) {
                injectBegin = i;
            }
            else if (injectBegin != -1 && insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.PUTFIELD
                && ((FieldInsnNode) insn).owner.equals(classNode.name) && ((FieldInsnNode) insn).name.equals(playerCloakUrlField)
            ) {
                LabelNode skip = new LabelNode();
                AbstractInsnNode begin = methodNode.instructions.get(injectBegin);
                methodNode.instructions.insertBefore(begin, new JumpInsnNode(Opcodes.GOTO, skip));
                
                InsnList list = new InsnList();
                list.add(skip);
                list.add(new FieldInsnNode(Opcodes.GETSTATIC, "dev/su5ed/legacyfix/skins/PlayerTextures", "INSTANCE", "Ldev/su5ed/legacyfix/skins/PlayerTextures;"));
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, usernameField, "Ljava/lang/String;"));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, stringUtilsClass, stripControlCodesMethod, "(Ljava/lang/String;)Ljava/lang/String;"));
                list.add(new FieldInsnNode(Opcodes.GETSTATIC, "dev/su5ed/legacyfix/skins/TextureType", "CAPE", "Ldev/su5ed/legacyfix/skins/TextureType;"));
                list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "dev/su5ed/legacyfix/skins/PlayerTextures", "getTextureURL", "(Ljava/lang/String;Ldev/su5ed/legacyfix/skins/TextureType;)Ljava/lang/String;"));
                methodNode.instructions.insertBefore(insn, list);
                
                break;
            }
        }
    }
}
