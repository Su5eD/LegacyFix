package mods.su5ed.legacyfix;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class LegacyFixPlugin implements IFMLLoadingPlugin {
	
	public LegacyFixPlugin() {
		// Dirty hack to make sure our transformer gets registered sooner than miscperipherals
		LaunchClassLoader classLoader = ((LaunchClassLoader) getClass().getClassLoader());
		classLoader.registerTransformer("mods.su5ed.legacyfix.LegacyFixTransformer");
	}

	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return null;
	}

	@Override
	public String getModContainerClass() {
		return "mods.su5ed.legacyfix.LegacyFixModContainer";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		
	}
}
