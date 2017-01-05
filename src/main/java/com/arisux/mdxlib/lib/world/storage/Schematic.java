package com.arisux.mdxlib.lib.world.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.arisux.mdxlib.MDX;
import com.arisux.mdxlib.lib.world.CoordData;
import com.arisux.mdxlib.lib.world.CoordData.BlockDataStore;
import com.arisux.mdxlib.lib.world.CoordSelection;
import com.arisux.mdxlib.lib.world.Structure;
import com.arisux.mdxlib.lib.world.tile.TileEntities;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class Schematic
{
    private short width;
    private short height;
    private short length;
    private CoordData origin;
    private Block[] blocks;
    private byte[] metadata;
    private ArrayList<NBTTagCompound> tileEntityTags = new ArrayList<NBTTagCompound>();
    private Map<Short, String> idMap;
    private File file;

    public Schematic(File file, NBTTagCompound tagCompound)
    {
        String materials = tagCompound.getString("Materials");

        if (!materials.equals("Alpha"))
        {
            MDX.log().warn("Unsupported schematic format: " + file);
            return;
        }

        this.file = file;
        this.origin = new CoordData(tagCompound.getShort("WEOriginX"), tagCompound.getShort("WEOriginY"), tagCompound.getShort("WEOriginZ"));
        this.width = tagCompound.getShort("Width");
        this.height = tagCompound.getShort("Height");
        this.length = tagCompound.getShort("Length");
        this.metadata = tagCompound.getByteArray("Data");
        byte[] blockIds = tagCompound.getByteArray("Blocks");
        byte[] addBlocks = tagCompound.getByteArray("AddBlocks");
        NBTTagCompound mappings = tagCompound.getCompoundTag("Mapping");

        ///////////////////////////////////////////////
        // Load Mappings
        ///////////////////////////////////////////////
        Map<Short, String> idMap = new HashMap<Short, String>();

        for (Object obj : mappings.getKeySet())
        {
            String alias = obj.toString();
            short id = mappings.getShort(alias);
            idMap.put(id, alias);
        }

        if (!idMap.isEmpty())
        {
            System.out.println("Schematic is missing mappings: " + file);
        }

        this.idMap = idMap;

        ///////////////////////////////////////////////
        // Load Blocks
        ///////////////////////////////////////////////
        this.blocks = new Block[blockIds.length];

        for (int i = 0; i < blockIds.length; i++)
        {
            int blockID = blockIds[i] & 0xff;

            if (addBlocks.length >= (blockIds.length + 1) / 2)
            {
                boolean lowerNybble = (i & 1) == 0;
                blockID |= lowerNybble ? ((addBlocks[i >> 1] & 0x0F) << 8) : ((addBlocks[i >> 1] & 0xF0) << 4);
            }

            String key = this.idMap.get((short) blockID);
            Block block = null;
            block = key != null ? Block.getBlockFromName(key) : null;
            block = block == null ? Block.getBlockById(blockID) : block;

            this.blocks[i] = block;
        }

        ///////////////////////////////////////////////
        // Load Tile Entities
        ///////////////////////////////////////////////
        NBTTagList tileEntities = tagCompound.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tileEntities.tagCount(); i++)
        {
            tileEntityTags.add(tileEntities.getCompoundTagAt(i));
        }
    }

    public void addBlocksToQueue(Structure structure)
    {
        CoordSelection blockArea = CoordSelection.areaFromSize(new CoordData(0, 0, 0), new int[] { width, height, length });

        for (int pass = 0; pass < 2; pass++)
        {
            for (CoordData relative : blockArea)
            {
                int index = (int) relative.x + ((int) relative.y * length + (int) relative.z) * width;
                CoordData data = structure.getData().add(relative);
                Block block = blocks[index];
                byte meta = this.metadata[index];

                data.store(new BlockDataStore(block, meta));

                if (block != null && getPass(block, meta) == pass && block != net.minecraft.init.Blocks.air)
                {
                    structure.getBlockQueue().add(data);
                }
            }
        }
    }

    public void generateTileEntities(World world, CoordData data)
    {
        HashMap<Integer, TileEntity> tileEntities = new HashMap<Integer, TileEntity>();

        for (NBTTagCompound tag : tileEntityTags)
        {
            TileEntity tileEntity = TileEntity.createAndLoadEntity(tag);

            if (tileEntity != null)
            {
                tileEntities.put(new CoordData(tileEntity).hashCode(), tileEntity);
            }
        }

        CoordSelection blockArea = CoordSelection.areaFromSize(new CoordData(0, 0, 0), new int[] { width, height, length });

        for (int pass = 0; pass < 2; pass++)
        {
            for (CoordData relative : blockArea)
            {
                int index = (int) relative.x + ((int) relative.y * length + (int) relative.z) * width;
                Block block = blocks[index];
                byte meta = this.metadata[index];

                if (block != null && getPass(block, meta) == pass)
                {
                    CoordData pos = new CoordData(data.x, data.y, data.z).add(relative).store(new BlockDataStore(block, meta));
                    TileEntity tileEntity = tileEntities.get(relative.hashCode());

                    if (tileEntity != null)
                    {
                        world.setBlockMetadataWithNotify((int) pos.x, (int) pos.y, (int) pos.z, meta, 2);
                        TileEntities.setTileEntityPosition(tileEntity, pos);
                        world.setTileEntity((int) pos.x, (int) pos.y, (int) pos.z, tileEntity);
                        tileEntity.updateContainingBlockInfo();
                    }
                }
            }
        }
    }

    @Deprecated
    public void generate(World world, CoordData data)
    {
        HashMap<Integer, TileEntity> tileEntities = new HashMap<Integer, TileEntity>();

        for (NBTTagCompound tag : tileEntityTags)
        {
            TileEntity tileEntity = TileEntity.createAndLoadEntity(tag);

            if (tileEntity != null)
            {
                tileEntities.put(new CoordData(tileEntity).hashCode(), tileEntity);
            }
        }

        CoordSelection blockArea = CoordSelection.areaFromSize(new CoordData(0, 0, 0), new int[] { width, height, length });

        for (int pass = 0; pass < 2; pass++)
        {
            for (CoordData relative : blockArea)
            {
                int index = (int) relative.x + ((int) relative.y * length + (int) relative.z) * width;
                Block block = blocks[index];
                byte meta = this.metadata[index];

                if (block != null && getPass(block, meta) == pass)
                {
                    CoordData pos = new CoordData(data.x, data.y, data.z).add(relative).store(new BlockDataStore(block, meta));
                    world.setBlock((int) pos.x, (int) pos.y, (int) pos.z, block, meta, 3);

                    TileEntity tileEntity = tileEntities.get(relative.hashCode());

                    if (tileEntity != null)
                    {
                        world.setBlockMetadataWithNotify((int) pos.x, (int) pos.y, (int) pos.z, meta, 2);
                        TileEntities.setTileEntityPosition(tileEntity, pos);
                        world.setTileEntity((int) pos.x, (int) pos.y, (int) pos.z, tileEntity);
                        tileEntity.updateContainingBlockInfo();
                    }
                }
            }
        }
    }

    private int getPass(Block block, int metadata)
    {
        return (block.isNormalCube() || block.getMaterial() == Material.air) ? 0 : 1;
    }

    public File getFile()
    {
        return file;
    }

    public short width()
    {
        return this.width;
    }

    public short height()
    {
        return this.height;
    }

    public short length()
    {
        return this.length;
    }

    public Block[] blocks()
    {
        return this.blocks;
    }

    public byte[] metadata()
    {
        return this.metadata;
    }

    public CoordData origin()
    {
        return this.origin;
    }

    public ArrayList<NBTTagCompound> getTileEntities()
    {
        return this.tileEntityTags;
    }
}
