package mods.su5ed.legacyfix;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.ModMetadata;

public class LegacyFixModContainer extends DummyModContainer {
	
	public LegacyFixModContainer() {
		super(getModMetadata());
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
