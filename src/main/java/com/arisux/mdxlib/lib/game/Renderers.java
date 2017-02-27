package com.arisux.mdxlib.lib.game;

import java.util.HashMap;

import com.arisux.mdxlib.lib.client.render.ItemRenderer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Renderers implements IInitEvent
{
    public static Renderers                                    INSTANCE       = new Renderers();
    private final HashMap<ModelResourceLocation, ItemRenderer> ITEM_RENDERERS = new HashMap<ModelResourceLocation, ItemRenderer>();

    public static void register(Item item, ItemRenderer renderer)
    {
        INSTANCE.ITEM_RENDERERS.put(new ModelResourceLocation(item.getRegistryName(), "inventory"), renderer);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        for (ModelResourceLocation resource : ITEM_RENDERERS.keySet())
        {
            event.getModelRegistry().putObject(resource, ITEM_RENDERERS.get(resource));
        }
    }
}
