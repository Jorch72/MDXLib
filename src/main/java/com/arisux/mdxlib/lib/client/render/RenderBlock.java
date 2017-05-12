package com.arisux.mdxlib.lib.client.render;

import com.arisux.mdxlib.lib.world.block.BlockShape;
import com.arisux.mdxlib.lib.world.block.BlockShape.Shape;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

@SuppressWarnings("unused")
public class RenderBlock
{
    public Tessellator   tessellator;
    public VertexBuffer  buffer;
    private Block        baseBlock;
    private IBlockAccess blockAccess;
    private Matrix3      rotation;
    private double       posX, posY, posZ;
    private double       vertexPosX, vertexPosY, vertexPosZ;
    private double       texU, texV;
    private double       minU, minV;
    private double       iconWidth, iconHeight;
    private float        r, g, b;
    private float        faceLightValue;
    private int          blockBrightness;
    private int          faceBrightness;
    private Vertex       faceU, faceV, faceN;

    public RenderBlock()
    {
        this.tessellator = Tessellator.getInstance();
        this.buffer = this.tessellator.getBuffer();
    }

    public void renderBlock(IBlockState blockstate)
    {
        Shape shape = blockstate.getBlock() instanceof BlockShape ? ((BlockShape) blockstate.getBlock()).getShape() : Shape.STANDARD;
        renderBlock(blockstate, shape);
    }
    
    public void renderBlock(IBlockState blockstate, Shape shape)
    {
        Draw.startQuads();
        render(null, blockstate, 0, 0, 0, shape);
        Draw.tessellate();
    }

    public boolean render(IBlockAccess world, IBlockState blockstate, int x, int y, int z, Shape shape)
    {
        Block block = blockstate.getBlock();
        baseBlock = block;
        blockAccess = world;
        posX = x + 0.5;
        posY = y + 0.5;
        posZ = z + 0.5;
        
        int metadata = 0;
        boolean flipped = (metadata >> 2) != 0;
        Matrix3 flip = Matrix3.rot(flipped ? 180 : 0, 1, ((metadata & 3) != 1 && (metadata & 3) != 3) ? 0 : 2);
        rotation = Matrix3.rotations[metadata & 3].mul(flip);

        minU = 0F;
        minV = 0F;
        iconWidth = 1F - minU;
        iconHeight = 1F - minV;

        if (world != null)
        {
            blockBrightness = baseBlock.getDefaultState().getPackedLightmapCoords(blockAccess, new BlockPos(x, y, z));
        }
        else
        {
            blockBrightness = 0xF000F0;
        }
        
        setColorMultiplier(0xFFFFFF);

        switch (shape)
        {
            case SLOPE:
                renderSlope();
                break;
            case CORNER:
                renderCorner();
                break;
            case INVERTED_CORNER:
                renderInvertedCorner();
                break;
            case RIDGE:
                renderRidge();
                break;
            case SMART_RIDGE:
                renderSmartRidge();
                break;
            case INVERTED_RIDGE:
                renderValley();
                break;
            case SMART_INVERTED_RIDGE:
                renderSmartValley();
                break;
            case STANDARD:
                renderStandard();
                break;
            default:
                break;
        }

        return true;
    }

    public void setColorMultiplier(int color)
    {
        r = ((color >> 16) & 0xff) / 255.0F;
        g = ((color >> 8) & 0xff) / 255.0F;
        b = (color & 0xff) / 255.0F;
    }

    public void beginTopFace()
    {
        beginOuterFace(Vertex.unitZ, Vertex.unitX, Vertex.unitY);
    }

    public void beginBottomFace()
    {
        beginOuterFace(Vertex.unitX, Vertex.unitZ, Vertex.unitNY);
    }

    public void beginPosXFace()
    {
        beginOuterFace(Vertex.unitY, Vertex.unitZ, Vertex.unitX);
    }

    public void beginNegXFace()
    {
        beginOuterFace(Vertex.unitZ, Vertex.unitY, Vertex.unitNX);
    }

    public void beginPosZFace()
    {
        beginOuterFace(Vertex.unitX, Vertex.unitY, Vertex.unitZ);
    }

    public void beginNegZFace()
    {
        beginOuterFace(Vertex.unitY, Vertex.unitX, Vertex.unitNZ);
    }

    public void beginPosXSlope()
    {
        beginInnerFace(Vertex.unitPXPY);
    }

    public void beginNegXSlope()
    {
        beginInnerFace(Vertex.unitNXPY);
    }

    public void beginPosZSlope()
    {
        beginInnerFace(Vertex.unitPYPZ);
    }

    public void beginNegZSlope()
    {
        beginInnerFace(Vertex.unitPYNZ);
    }

    public void beginInnerFace(Vertex normal)
    {
        beginFace(normal);
        lightFace(faceLightValue, blockBrightness);
    }

    public void beginOuterFace(Vertex u, Vertex v, Vertex normal)
    {
        beginFace(normal);

        if (blockAccess == null)
        {
            shadeFace(faceLightValue);
        }
        else if (Minecraft.isAmbientOcclusionEnabled())
        {
            faceU = rotation.mul(u);
            faceV = rotation.mul(v);
        }
        else
        {
            lightFace(faceLightValue, faceBrightness);
        }
    }

    public void beginFace(Vertex v)
    {
        faceN = rotation.mul(v);
        normal(faceN.x, faceN.y, faceN.z);
        Vertex vertex = faceN.add(posX, posY, posZ);

        if (blockAccess != null)
        {
            BlockPos pos = new BlockPos((int) Math.floor(vertex.x), (int) Math.floor(vertex.y), (int) Math.floor(vertex.z));
            faceBrightness = baseBlock.getDefaultState().getPackedLightmapCoords(blockAccess, pos);
        }

        double nx = faceN.x * faceN.x;
        double ny = faceN.y * faceN.y;
        double nz = faceN.z * faceN.z;
        faceLightValue = (float) (0.6 * nx * nx + 0.8 * nz + (faceN.y > 0 ? 1 : 0.5) * ny);
    }

    public void normal(double x, double y, double z)
    {
        Vertex v = rotation.mul(x, y, z);
        buffer.normal(v.x, v.y, v.z);
    }

    public void corner(double x, double y, double z, double tx, double ty)
    {
        cornerOrInteriorVertex(x, y, z, tx, ty, true);
    }

    public void vertex(double x, double y, double z, double tx, double ty)
    {
        cornerOrInteriorVertex(x, y, z, tx, ty, false);
    }

    public void cornerOrInteriorVertex(double x, double y, double z, double tx, double ty, boolean corner)
    {
        Vertex p = rotation.mul(x - 0.5, y - 0.5, z - 0.5);
        vertexPosX = posX + p.x;
        vertexPosY = posY + p.y;
        vertexPosZ = posZ + p.z;

        if (blockAccess != null)
        {
            if (Minecraft.isAmbientOcclusionEnabled())
            {
                if (corner)
                {
                    lightCornerVertex(vertexPosX, vertexPosY, vertexPosZ);
                }
                else
                {
                    lightInteriorVertex(vertexPosX, vertexPosY, vertexPosZ);
                }
            }
        }

        texU = minU + tx * iconWidth;
        texV = minV + ty * iconHeight;
        Draw.vertex(vertexPosX, vertexPosY, vertexPosZ, texU, texV).endVertex();
    }

    public void addVertexWithUV()
    {
        Draw.vertex(vertexPosX, vertexPosY, vertexPosZ, texU, texV).endVertex();
    }

    public void lightCornerVertex(double x, double y, double z)
    {
        int aoBrightness, aoBrightnessSum = 0;
        float aoLightValueSum = 0;
        Vertex p = faceN.mul(0.5).add(x, y, z);

        for (int i = 0; i <= 1; i++)
        {
            for (int j = 0; j <= 1; j++)
            {
                Vertex q = p.add(faceU.mul(i - 0.5)).add(faceV.mul(j - 0.5));
                BlockPos pos = new BlockPos((int) Math.floor(q.x), (int) Math.floor(q.y), (int) Math.floor(q.z));
                aoBrightness = baseBlock.getDefaultState().getPackedLightmapCoords(blockAccess, pos);
                if (aoBrightness == 0)
                    aoBrightness = faceBrightness;
                aoBrightnessSum += aoBrightness;
                aoLightValueSum += blockAccess.getBlockState(pos).getAmbientOcclusionLightValue();
            }
        }

        lightFace(faceLightValue * aoLightValueSum / 4, (aoBrightnessSum >> 2) & 0xff00ff);
    }

    public void lightInteriorVertex(double xv, double yv, double zv)
    {
        int br = 0, brSum = 0, brCount = 0;

        for (int i = 0; i <= 1; i++)
        {
            for (int j = 0; j <= 1; j++)
            {
                for (int k = 0; k <= 1; k++)
                {
                    int xb = (int) Math.floor(xv - 0.5 + i);
                    int yb = (int) Math.floor(yv - 0.5 + j);
                    int zb = (int) Math.floor(zv - 0.5 + k);
                    BlockPos pos = new BlockPos(xb, yb, zb);
                    br = baseBlock.getDefaultState().getPackedLightmapCoords(blockAccess, pos);
                    
                    if (br != 0)
                    {
                        brSum += br;
                        brCount += 1;
                    }
                }
            }
        }

        if (brCount > 0)
        {
            int br1 = (brSum >> 16) / brCount;
            int br2 = (brSum & 0xffff) / brCount;
            br = (br1 << 16) | br2;
        }

        lightFace(faceLightValue, br);
    }

    public void lightFace(float bm, int br)
    {
//        tessellator.setBrightness(br);
        shadeFace(bm);
    }

    public void shadeFace(float bm)
    {
        buffer.color(bm * r, bm * g, bm * b, 255F);
    }

    public boolean hasNeighbour(int dx, int dy, int dz, Shape[] ridgeshapes)
    {
        return false;
    }

    public boolean ridgeAt(int dx, int dy, int dz)
    {
        return hasNeighbour(dx, dy, dz, Shape.ridges);
    }

    public boolean ridgeOrSlopeAt(int dx, int dy, int dz)
    {
        return hasNeighbour(dx, dy, dz, Shape.ridgesOrSlopes);
    }

    public boolean valleyAt(int dx, int dy, int dz)
    {
        return hasNeighbour(dx, dy, dz, Shape.invertedRidges);
    }

    public boolean valleyOrSlopeAt(int dx, int dy, int dz)
    {
        return hasNeighbour(dx, dy, dz, Shape.invertedRidgesOrSlopes);
    }

    /** ------------------------------------------------------------------------------ **/

    private void leftQuad()
    {
        beginPosXFace();
        corner(1, 1, 1, 0, 0);
        corner(1, 0, 1, 0, 1);
        corner(1, 0, 0, 1, 1);
        corner(1, 1, 0, 1, 0);
    }

    private void rightQuad()
    {
        beginNegXFace();
        corner(0, 1, 0, 0, 0);
        corner(0, 0, 0, 0, 1);
        corner(0, 0, 1, 1, 1);
        corner(0, 1, 1, 1, 0);
    }

    private void frontQuad()
    {
        beginNegZFace();
        corner(1, 1, 0, 0, 0);
        corner(1, 0, 0, 0, 1);
        corner(0, 0, 0, 1, 1);
        corner(0, 1, 0, 1, 0);
    }

    private void backQuad()
    {
        beginPosZFace();
        corner(0, 1, 1, 0, 0);
        corner(0, 0, 1, 0, 1);
        corner(1, 0, 1, 1, 1);
        corner(1, 1, 1, 1, 0);
    }

    private void bottomQuad()
    {
        beginBottomFace();
        corner(0, 0, 1, 0, 0);
        corner(0, 0, 0, 0, 1);
        corner(1, 0, 0, 1, 1);
        corner(1, 0, 1, 1, 0);
    }
    
    private void topQuad()
    {
        beginTopFace();
        corner(0, 1, 1, 0, 0);
        corner(0, 1, 0, 0, 1);
        corner(1, 1, 0, 1, 1);
        corner(1, 1, 1, 1, 0);
    }

    private void leftTriangle()
    {
        beginPosXFace();
        corner(1, 1, 1, 0, 0);
        corner(1, 0, 1, 0, 1);
        corner(1, 0, 0, 1, 1);
        corner(1, 1, 1, 0, 0);
    }

    private void rightTriangle()
    {
        beginNegXFace();
        corner(0, 1, 1, 1, 0);
        corner(0, 0, 0, 0, 1);
        corner(0, 0, 1, 1, 1);
        addVertexWithUV();
    }

    private void ridgeLeftFace()
    {
        beginPosXFace();
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        corner(1, 0, 1, 0, 1);
        corner(1, 0, 0, 1, 1);
        addVertexWithUV();
    }

    private void ridgeRightFace()
    {
        beginNegXFace();
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        corner(0, 0, 0, 0, 1);
        corner(0, 0, 1, 1, 1);
        addVertexWithUV();
    }

    private void ridgeBackFace()
    {
        beginPosZFace();
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        corner(0, 0, 1, 0, 1);
        corner(1, 0, 1, 1, 1);
        addVertexWithUV();
    }

    private void ridgeFrontFace()
    {
        beginNegZFace();
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        corner(1, 0, 0, 0, 1);
        corner(0, 0, 0, 1, 1);
        addVertexWithUV();
    }

    private void ridgeFrontSlope()
    {
        beginNegZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 0, 0, 1);
        vertex(0, 0, 0, 1, 1);
        addVertexWithUV();
    }

    private void ridgeBackSlope()
    {
        beginPosZSlope();
        vertex(0, 0.5, 0.5, 0, 0.5);
        vertex(0, 0, 1, 0, 1);
        vertex(1, 0, 1, 1, 1);
        vertex(1, 0.5, 0.5, 1, 0.5);
    }

    private void ridgeLeft()
    {
        if (ridgeOrSlopeAt(1, 0, 0))
        {
            connectRidgeLeft();
        }
        else
        {
            beginPosXSlope();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(1, 0, 1, 0, 1);
            vertex(1, 0, 0, 1, 1);
            addVertexWithUV();
        }
    }

    private void connectRidgeLeft()
    {
        beginNegZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0.5, 0.5, 0, 0.5);
        vertex(1, 0, 0, 0, 1);
        addVertexWithUV();
        beginPosZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 1, 1, 1);
        vertex(1, 0.5, 0.5, 1, 0.5);
        addVertexWithUV();
    }

    private void ridgeRight()
    {
        if (ridgeOrSlopeAt(-1, 0, 0))
        {
            connectRidgeRight();
        }
        else
        {
            beginNegXSlope();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(0, 0, 0, 0, 1);
            vertex(0, 0, 1, 1, 1);
            addVertexWithUV();
        }
    }

    private void connectRidgeRight()
    {
        beginNegZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0, 0, 1, 1);
        vertex(0, 0.5, 0.5, 1, 0.5);
        addVertexWithUV();
        beginPosZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0.5, 0.5, 0, 0.5);
        vertex(0, 0, 1, 0, 1);
        addVertexWithUV();
    }

    private void ridgeFront()
    {
        if (ridgeOrSlopeAt(0, 0, -1))
        {
            connectRidgeFront();
        }
        else
        {
            beginNegZSlope();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(1, 0, 0, 0, 1);
            vertex(0, 0, 0, 1, 1);
            addVertexWithUV();
        }
    }

    private void connectRidgeFront()
    {
        beginPosXSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 0, 1, 1);
        vertex(0.5, 0.5, 0, 1, 0.5);
        addVertexWithUV();
        beginNegXSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 0, 0, 0.5);
        vertex(0, 0, 0, 0, 1);
        addVertexWithUV();
    }

    private void ridgeBack()
    {
        if (ridgeOrSlopeAt(0, 0, 1))
        {
            connectRidgeBack();
        }
        else
        {
            beginPosZSlope();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(0, 0, 1, 0, 1);
            vertex(1, 0, 1, 1, 1);
            addVertexWithUV();
        }
    }

    private void connectRidgeBack()
    {
        beginPosXSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 1, 0, 0.5);
        vertex(1, 0, 1, 0, 1);
        addVertexWithUV();
        beginNegXSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0, 1, 1, 1);
        vertex(0.5, 0.5, 1, 1, 0.5);
        addVertexWithUV();
    }

    private void connectValleyLeft()
    {
        beginPosZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0.5, 0.5, 1, 0.5);
        vertex(1, 1, 0, 1, 0);
        addVertexWithUV();
        beginNegZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 1, 1, 0, 0);
        vertex(1, 0.5, 0.5, 0, 0.5);
        addVertexWithUV();
        valleyEndLeft();
    }

    private void connectValleyRight()
    {
        beginPosZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 1, 0, 0, 0);
        vertex(0, 0.5, 0.5, 0, 0.5);
        addVertexWithUV();
        beginNegZSlope();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0.5, 0.5, 1, 0.5);
        vertex(0, 1, 1, 1, 0);
        addVertexWithUV();
        valleyEndRight();
    }

    private void connectValleyFront()
    {
        beginPosXSlope();
        vertex(0, 1, 0, 1, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 0, 1, 0.5);
        addVertexWithUV();
        beginNegXSlope();
        vertex(1, 1, 0, 0, 0);
        vertex(0.5, 0.5, 0, 0, 0.5);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
        valleyEndFront();
    }

    private void connectValleyBack()
    {
        beginPosXSlope();
        vertex(0, 1, 1, 0, 0);
        vertex(0.5, 0.5, 1, 0, 0.5);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
        beginNegXSlope();
        vertex(1, 1, 1, 1, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 1, 1, 0.5);
        addVertexWithUV();
        valleyEndBack();
    }

    private void valleyEndLeft()
    {
        beginPosXFace();
        corner(1, 1, 1, 0, 0);
        corner(1, 0, 1, 0, 1);
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
        corner(1, 0, 1, 0, 1);
        corner(1, 0, 0, 1, 1);
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
        corner(1, 0, 0, 1, 1);
        corner(1, 1, 0, 1, 0);
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
    }

    private void valleyEndRight()
    {
        beginNegXFace();
        corner(0, 0, 1, 1, 1);
        corner(0, 1, 1, 1, 0);
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
        corner(0, 0, 0, 0, 1);
        corner(0, 0, 1, 1, 1);
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
        corner(0, 1, 0, 0, 0);
        corner(0, 0, 0, 0, 1);
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();
    }

    private void valleyEndFront()
    {
        beginNegZFace();
        corner(1, 1, 0, 0, 0);
        corner(1, 0, 0, 0, 1);
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        addVertexWithUV();
        corner(1, 0, 0, 0, 1);
        corner(1, 0, 0, 1, 1);
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        addVertexWithUV();
        corner(0, 0, 0, 1, 1);
        corner(0, 1, 0, 1, 0);
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        addVertexWithUV();
    }

    private void valleyEndBack()
    {
        beginPosZFace();
        corner(0, 1, 1, 0, 0);
        corner(0, 0, 1, 0, 1);
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        addVertexWithUV();
        corner(0, 0, 1, 0, 1);
        corner(1, 0, 1, 1, 1);
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        addVertexWithUV();
        corner(1, 0, 1, 1, 1);
        corner(1, 1, 1, 1, 0);
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        addVertexWithUV();
    }

    private void smartValleyLeft()
    {
        if (valleyOrSlopeAt(1, 0, 0))
        {
            connectValleyLeft();
        }
        else
        {
            terminateValleyLeft();
        }
    }

    private void terminateValleyLeft()
    {
        beginNegXSlope();
        vertex(1, 1, 0, 0, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 1, 1, 1, 0);
        addVertexWithUV();
        leftQuad();
    }

    private void smartValleyRight()
    {
        if (valleyOrSlopeAt(-1, 0, 0))
        {
            connectValleyRight();
        }
        else
        {
            terminateValleyRight();
        }
    }

    private void terminateValleyRight()
    {
        beginPosXSlope();
        vertex(0, 1, 1, 0, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 1, 0, 1, 0);
        addVertexWithUV();
        rightQuad();
    }

    private void smartValleyFront()
    {
        if (valleyOrSlopeAt(0, 0, -1))
        {
            connectValleyFront();
        }
        else
        {
            terminateValleyFront();
        }
    }

    private void terminateValleyFront()
    {
        beginPosZSlope();
        vertex(0, 1, 0, 0, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 1, 0, 1, 0);
        addVertexWithUV();
        frontQuad();
    }

    private void smartValleyBack()
    {
        if (valleyOrSlopeAt(0, 0, 1))
        {
            connectValleyBack();
        }
        else
        {
            terminateValleyBack();
        }
    }

    private void terminateValleyBack()
    {
        beginNegZSlope();
        vertex(1, 1, 1, 0, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 1, 1, 1, 0);
        addVertexWithUV();
        backQuad();
    }

    /** ------------------------------------------------------------------------------ **/

    private void renderStandard()
    {
        rightQuad();
        leftQuad();
        bottomQuad();
        frontQuad();
        backQuad();
        topQuad();
    }

    private void renderSlope()
    {
        beginNegZSlope();

        if (valleyAt(0, 0, 1))
        {
            vertex(1, 1, 1, 0, 0);
            vertex(1, 0, 0, 0, 1);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            addVertexWithUV();
            vertex(1, 0, 0, 0, 1);
            vertex(0, 0, 0, 1, 1);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            addVertexWithUV();
            vertex(0, 0, 0, 1, 1);
            vertex(0, 1, 1, 1, 0);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            addVertexWithUV();
            connectValleyBack();
        }
        else
        {
            vertex(1, 1, 1, 0, 0);
            vertex(1, 0, 0, 0, 1);
            vertex(0, 0, 0, 1, 1);
            vertex(0, 1, 1, 1, 0);
            backQuad();
        }

        leftTriangle();
        rightTriangle();
        bottomQuad();

        if (ridgeAt(0, 0, -1))
        {
            connectRidgeFront();
        }
    }

    private void renderCorner()
    {
        // Front slope
        beginNegZSlope();
        vertex(0, 1, 1, 1, 0);
        vertex(1, 0, 0, 0, 1);
        vertex(0, 0, 0, 1, 1);
        addVertexWithUV();
        // Left slope
        beginPosXSlope();
        vertex(0, 1, 1, 0, 0);
        vertex(1, 0, 1, 0, 1);
        vertex(1, 0, 0, 1, 1);
        addVertexWithUV();
        // Back
        beginNegZFace();
        corner(0, 1, 1, 0, 0);
        corner(0, 0, 1, 0, 1);
        corner(1, 0, 1, 1, 1);
        addVertexWithUV();
        // Other faces
        rightTriangle();
        bottomQuad();

        if (ridgeAt(0, 0, -1))
        {
            connectRidgeFront();
        }
        if (ridgeAt(1, 0, 0))
        {
            connectRidgeLeft();
        }
    }

    private void renderInvertedCorner()
    {
        // Left slope
        beginPosXSlope();
        vertex(0, 1, 0, 1, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 0, 1, 1);
        addVertexWithUV();

        if (valleyAt(-1, 0, 0))
        {
            connectValleyRight();
        }
        else
        {
            terminateValleyRight();
        }

        // Front slope
        beginNegZSlope();
        vertex(1, 1, 1, 0, 0);
        vertex(1, 0, 0, 0, 1);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        addVertexWithUV();

        if (valleyAt(0, 0, 1))
        {
            connectValleyBack();
        }
        else
        {
            terminateValleyBack();
        }

        // Front triangle
        beginNegZFace();
        corner(0, 1, 0, 1, 0);
        corner(1, 0, 0, 0, 1);
        corner(0, 0, 0, 1, 1);
        addVertexWithUV();
        // Other faces
        leftTriangle();
        bottomQuad();
    }

    private void renderRidge()
    {
        // Front slope
        beginNegZSlope();
        vertex(1, 0.5, 0.5, 0, 0.5);
        vertex(1, 0, 0, 0, 1);
        vertex(0, 0, 0, 1, 1);
        vertex(0, 0.5, 0.5, 1, 0.5);
        // Other faces
        ridgeBackSlope();
        ridgeLeftFace();
        ridgeRightFace();
        ridgeFront();
        ridgeBack();
        bottomQuad();
    }

    private void renderSmartRidge()
    {
        ridgeLeft();
        ridgeRight();
        ridgeBack();
        ridgeFront();
        bottomQuad();
    }

    private void renderValley()
    {
        connectValleyLeft();
        connectValleyRight();
        smartValleyFront();
        smartValleyBack();
        bottomQuad();
    }

    private void renderSmartValley()
    {
        smartValleyLeft();
        smartValleyRight();
        smartValleyFront();
        smartValleyBack();
        bottomQuad();
    }
}
