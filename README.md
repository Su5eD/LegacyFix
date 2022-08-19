# LegacyFix

Fixes some old mods to work on the new launcher.
This is a coremod, and thus must be put into the **coremods** folder to load.

### NEI
`TMIUninstaller#getJarFile` searches for `minecraft.jar` on the classpath, which doesn't exist on the modern 
launcher. This results in a `NullPointerException` and prevents NEI from loading.

### Skins

Patches skin display using Minecraft's official APIs.