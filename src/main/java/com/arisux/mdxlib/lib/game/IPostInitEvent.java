package com.arisux.mdxlib.lib.game;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public interface IPostInitEvent
{
    @Mod.EventHandler
    public void post(FMLPostInitializationEvent event);
}
