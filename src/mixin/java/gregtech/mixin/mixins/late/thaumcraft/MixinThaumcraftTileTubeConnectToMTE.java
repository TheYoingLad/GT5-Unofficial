package gregtech.mixin.mixins.late.thaumcraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.mixin.hooks.ThaumcraftConnectToMTEHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.tiles.TileTube;

@Mixin(value = TileTube.class, remap = false)
public abstract class MixinThaumcraftTileTubeConnectToMTE extends TileThaumcraft {

    @Redirect(
        method = "/(calculateSuction)|(checkVenting)|(equalizeWithNeighbours)/",
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

    @Definition(id = "IEssentiaTransport", type = IEssentiaTransport.class)
    @Expression("(IEssentiaTransport) ?")
    @WrapOperation(
        method = "/(calculateSuction)|(checkVenting)|(equalizeWithNeighbours)/",
        at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public IEssentiaTransport gt5u$IEssentiaTransportWrap(Object te, Operation<IEssentiaTransport> cast) {
        if (te instanceof BaseMetaTileEntity bmte) return cast.call(bmte.getMetaTileEntity());
        return cast.call(te);
    }

    @Inject(method = "canConnectSide", at = @At("RETURN"), cancellable = true)
    protected void gt5$canConnectSide(int side, CallbackInfoReturnable<Boolean> cir,
        @Local(name = "tile") TileEntity tile) {
        cir.setReturnValue(
            cir.getReturnValue()
                || (tile instanceof BaseMetaTileEntity bmte && bmte.getMetaTileEntity() instanceof IEssentiaTransport));
        cir.cancel();
    }
}
