package mods.su5ed.legacyfix;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.versioning.ArtifactVersion;

import java.util.List;
import java.util.Set;

public class LegacyFixModContainer extends DummyModContainer {
	
	public LegacyFixModContainer() {
		super(getModMetadata());
	}

	@Override
	public List<ArtifactVersion> getDependencies() {
		return getMetadata().dependencies;
	}

	@Override
	public Set<ArtifactVersion> getRequirements() {
		return getModMetadata().requiredMods;
	}

	private static ModMetadata getModMetadata() {
		MetadataCollection metadata = MetadataCollection.from(LegacyFixModContainer.class.getResourceAsStream("/legacyfixmod.info"), "LegacyFix");
		return metadata.getMetadataForId("legacyfix", null);
	}

	@Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;        
    }
}
