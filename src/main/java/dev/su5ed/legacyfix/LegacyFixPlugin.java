package dev.su5ed.legacyfix;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class LegacyFixPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "dev.su5ed.legacyfix.NEITransformer", "dev.su5ed.legacyfix.PlayerSkinTransformer" };
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
