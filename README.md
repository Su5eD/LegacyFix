# LegacyFix
Fixes NEI to work on the new launcher, and MiscPeripherals to work with ASM 4.1+.
This is a coremod, and thus must be put into the **coremods** folder to load.

### NEI
`TMIUninstaller#getJarFile` searches for `minecraft.jar` on the classpath, which doesn't exist on the 
modern launcher. This in a `NullPointerException` and prevents NEI from loading.

### MiscPeripherals
Due to a programming error, `BlockTurtleTransformer` and `TileEntityTurtleTransformer` pass in `4` instead of 
`Opcodes.ASM4` (which's actual value is `4 << 16 | 0 << 8 | 0`) into the constructor of `MethodInsnNode` as the api 
number, what throws an exception and kills the transformer.
