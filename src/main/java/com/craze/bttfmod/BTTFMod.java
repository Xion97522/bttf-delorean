package com.craze.bttfmod;

import com.craze.bttfmod.init.ModEntityTypes;
import com.craze.bttfmod.init.ModItems;
import com.craze.bttfmod.init.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(BTTFMod.MODID)
public class BTTFMod {
    public static final String MODID = "bttfmod";
    public static final Logger LOGGER = LogManager.getLogger();

    public BTTFMod() {
        GeckoLib.initialize();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntityTypes.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
