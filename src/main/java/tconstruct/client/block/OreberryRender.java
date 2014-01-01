package tconstruct.client.block;

import tconstruct.client.TProxyClient;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class OreberryRender implements ISimpleBlockRenderingHandler
{
    public static int model = RenderingRegistry.getNextAvailableRenderId();

    @Override
    public boolean renderWorldBlock (IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {

        if (modelId == model)
        {
            int md = world.getBlockMetadata(x, y, z);
            if (md < 4)
            {
                renderer.func_147782_a(0.25F, 0.0F, 0.25F, 0.75F, 0.5F, 0.75F);
                renderer.func_147784_q(block, x, y, z);
            }
            else if (md < 8)
            {
                renderer.func_147782_a(0.125F, 0.0F, 0.125F, 0.875F, 0.75F, 0.875F);
                renderer.func_147784_q(block, x, y, z);
            }
            else
            {
                renderer.func_147782_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                renderer.func_147784_q(block, x, y, z);
            }
        }
        return true;
    }

    @Override
    public void renderInventoryBlock (Block block, int metadata, int modelID, RenderBlocks renderer)
    {
        if (modelID == model)
        {
            renderer.func_147782_a(0.125F, 0.0F, 0.125F, 0.875F, 0.75F, 0.875F);
            TProxyClient.renderStandardInvBlock(renderer, block, metadata);
        }
    }

    @Override
    public boolean shouldRender3DInInventory (int modelID)
    {
        return true;
    }

    @Override
    public int getRenderId ()
    {
        return model;
    }
}
