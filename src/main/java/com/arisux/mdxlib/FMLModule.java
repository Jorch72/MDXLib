package com.arisux.mdxlib;

import java.util.Map;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value = MinecraftForge.MC_VERSION)
public class FMLModule implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { AccessTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public String getAccessTransformerClass()
    {
        return (AccessTransformer.class).getName();
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        ;
    }
}
