package com.arisux.mdx.lib.game;

import com.arisux.mdx.Console;
import com.arisux.mdx.MDXModule;
import com.arisux.mdx.commands.CommandBlockUpdate;
import com.arisux.mdx.commands.CommandGenerate;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommandHandler implements IInitEvent
{
    public static final CommandHandler instance = new CommandHandler();
    public CommandGenerate generate;
    public CommandBlockUpdate blockUpdate;

    @Override
    public void init(FMLInitializationEvent event)
    {
        if (!MDXModule.prefetchComplete)
        {
            Console.modificationWarning();
            return;
        }
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        if (!MDXModule.prefetchComplete)
        {
            Console.modificationWarning();
            return;
        }
        
        event.registerServerCommand(this.generate = new CommandGenerate());
        event.registerServerCommand(this.blockUpdate = new CommandBlockUpdate());
    }
}
