package com.arisux.mdxlib.lib.world.block;

import java.util.ArrayList;
import java.util.Arrays;

import com.arisux.mdxlib.MDX;
import com.arisux.mdxlib.lib.world.Pos;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Blocks
{
    public static ArrayList<Pos> getCoordDataInRange(int posX, int posY, int posZ, int range)
    {
        ArrayList<Pos> data = new ArrayList<Pos>();

        for (int x = posX - range; x < posX + range * 2; x++)
        {
            for (int y = posY - range; y < posY + range * 2; y++)
            {
                for (int z = posZ - range; z < posZ + range * 2; z++)
                {
                    data.add(new Pos(x, y, z));
                }
            }
        }

        return data;
    }

    public static ArrayList<Pos> getCoordDataInRangeIncluding(int posX, int posY, int posZ, int range, World world, Block... types)
    {
        ArrayList<Pos> data = new ArrayList<Pos>();

        for (int x = posX - range; x < posX + range * 2; x++)
        {
            for (int y = posY - range; y < posY + range * 2; y++)
            {
                for (int z = posZ - range; z < posZ + range * 2; z++)
                {
                    Pos coordData = new Pos(x, y, z);
                    Block block = coordData.getBlock(world);

                    if (Arrays.asList(types).contains(block))
                    {
                        data.add(coordData);
                    }
                }
            }
        }

        return data;
    }

    public static ArrayList<Pos> getCoordDataInRangeExcluding(int posX, int posY, int posZ, int range, World world, Block... types)
    {
        ArrayList<Pos> data = new ArrayList<Pos>();

        for (int x = posX - range; x < posX + range * 2; x++)
        {
            for (int y = posY - range; y < posY + range * 2; y++)
            {
                for (int z = posZ - range; z < posZ + range * 2; z++)
                {
                    Pos coordData = new Pos(x, y, z);
                    Block block = coordData.getBlock(world);

                    if (!Arrays.asList(types).contains(block))
                    {
                        data.add(coordData);
                    }
                }
            }
        }

        return data;
    }

    public static String getDomain(Block block)
    {
        String domain = "minecraft:";

        if (block.getUnlocalizedName().contains(":"))
        {
            domain = (block.getUnlocalizedName().split(":")[0] + ":").replace("tile.", "");
        }

        return domain;
    }

    public static void setCreativeTab(Block block, CreativeTabs tab)
    {
        if (tab != null)
        {
            block.setCreativeTab(tab);
        }
    }

    public static float getBlockResistance(Block blockParent)
    {
        return MDX.access().getBlockResistance(blockParent);
    }

    public static float getBlockHardness(Block blockParent)
    {
        return MDX.access().getBlockResistance(blockParent);
    }

    /**
     * @param block - Block to get the ResourceLocation from
     * @param side - Side to get the ResourceLocation from
     * @return The ResourceLocation of the side of the specified Block
     */
    @Deprecated
    @SideOnly(Side.CLIENT)
    public static ResourceLocation getBlockTexture(Block block, int side)
    {
        // IIcon icon = block.getBlockTextureFromSide(side);
        // return new ResourceLocation(getDomain(block).replace(":", ""), "textures/blocks/" + icon.getIconName().replace(getDomain(block), "") + ".png");
        return new ResourceLocation(getDomain(block).replace(":", ""), "textures/blocks/" + block.getUnlocalizedName().replace(getDomain(block), "") + ".png");
    }
}
