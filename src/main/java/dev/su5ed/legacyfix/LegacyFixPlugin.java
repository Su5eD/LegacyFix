package dev.su5ed.legacyfix;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.RelaunchClassLoader;

import java.util.Map;

public class LegacyFixPlugin implements IFMLLoadingPlugin {
	
	public LegacyFixPlugin() {
		// Dirty hack to make sure our transformer gets registered sooner than miscperipherals
		RelaunchClassLoader classLoader = ((RelaunchClassLoader) getClass().getClassLoader());
		classLoader.registerTransformer("dev.su5ed.legacyfix.LegacyFixTransformer");
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
		return "dev.su5ed.legacyfix.LegacyFixModContainer";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		
	}
}
