package com.craze.bttfmod.init;

import com.craze.bttfmod.BTTFMod;
import com.craze.bttfmod.item.DeLoreanSpawnItem;
import com.craze.bttfmod.item.FluxCapacitorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BTTFMod.MODID);

    public static final RegistryObject<Item> DELOREAN_SPAWN =
            ITEMS.register("delorean_spawn",
                    () -> new DeLoreanSpawnItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FLUX_CAPACITOR =
            ITEMS.register("flux_capacitor",
                    () -> new FluxCapacitorItem(new Item.Properties().stacksTo(1)));
}
