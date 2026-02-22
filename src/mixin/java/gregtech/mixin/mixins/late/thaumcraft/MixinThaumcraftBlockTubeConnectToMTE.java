package gregtech.mixin.mixins.late.thaumcraft;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.mixin.hooks.ThaumcraftConnectToMTEHelper;
import thaumcraft.common.blocks.BlockTube;

@Mixin(value = BlockTube.class, remap = false)
public abstract class MixinThaumcraftBlockTubeConnectToMTE extends BlockContainer {

    protected MixinThaumcraftBlockTubeConnectToMTE(Material p_i45386_1_) {
        super(p_i45386_1_);
    }

    @Redirect(
        method = "/(getSelectedBoundingBoxFromPool)|(addCollisionBoxesToList)/",
        at = @At(
            value = "INVOKE",
            target = "Lthaumcraft/api/ThaumcraftApiHelper;" + "getConnectableTile("
                + "Lnet/minecraft/world/World;"
                + "III"
                + "Lnet/minecraftforge/common/util/ForgeDirection;)"
                + "Lnet/minecraft/tileentity/TileEntity;"),
        require = 1)
    TileEntity gt5u$getConnectableTileRedirect(World world, int x, int y, int z, ForgeDirection face) {
        return ThaumcraftConnectToMTEHelper.getConnectableTile(world, x, y, z, face);
    }
}
