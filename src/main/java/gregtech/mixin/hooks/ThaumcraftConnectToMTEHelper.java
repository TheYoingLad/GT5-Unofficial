package gregtech.mixin.hooks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import thaumcraft.api.aspects.IEssentiaTransport;

public final class ThaumcraftConnectToMTEHelper {

    public static TileEntity getConnectableTile(World world, int x, int y, int z, ForgeDirection face) {
        TileEntity te = world.getTileEntity(x + face.offsetX, y + face.offsetY, z + face.offsetZ);
        if (te instanceof IEssentiaTransport) return te;
        if (te instanceof BaseMetaTileEntity bmte && bmte.getMetaTileEntity() instanceof IEssentiaTransport) return te;
        return null;
    }
}
