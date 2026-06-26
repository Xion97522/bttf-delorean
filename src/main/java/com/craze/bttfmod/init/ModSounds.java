package com.craze.bttfmod.init;

import com.craze.bttfmod.BTTFMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BTTFMod.MODID);

    public static final RegistryObject<SoundEvent> DELOREAN_ENGINE =
            SOUNDS.register("delorean_engine",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(BTTFMod.MODID, "delorean_engine")));

    public static final RegistryObject<SoundEvent> TIME_TRAVEL =
            SOUNDS.register("time_travel",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(BTTFMod.MODID, "time_travel")));
}
