# LegacyFix
Fixes old mods to work on the new launcher.
This is a coremod, and thus must be put into the **coremods** folder to load.

### NEI
`TMIUninstaller#getJarFile` searches for `minecraft.jar` on the classpath, which doesn't exist on the modern 
launcher. This results in a `NullPointerException` and prevents NEI from loading.

### MiscPeripherals
Due to a programming error, `BlockTurtleTransformer` and `TileEntityTurtleTransformer` pass in `4` instead of 
`Opcodes.ASM4` (which's actual value is `4 << 16 | 0 << 8 | 0`) into the constructor of `MethodInsnNode` as the api 
number, what throws an exception and kills the transformer.  
This only occurs on ASM 4.1+, which introduced code to verify the api number.

### ImmibisCore
`ImmibisCore` tries to register `MultiInterfaceClassTransformer` directly to `RelaunchClassLoader`, which gets 
renamed during runtime to `LaunchClassLoader`, meaning it no longer accepts instances of 
`cpw/mods/fml/relauncher/IClassTransformer`. A `ClassCastException` is thrown, and the transformer is never registred.
