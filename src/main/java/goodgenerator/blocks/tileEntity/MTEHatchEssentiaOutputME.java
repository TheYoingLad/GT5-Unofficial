package goodgenerator.blocks.tileEntity;

import static goodgenerator.loader.Loaders.magicCasing;
import static net.minecraft.util.StatCollector.translateToLocal;
import static thaumicenergistics.common.storage.AEEssentiaStackType.ESSENTIA_STACK_TYPE;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import goodgenerator.main.GoodGenerator;
import goodgenerator.util.ItemRefer;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.render.TextureFactory;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class MTEHatchEssentiaOutputME extends MTEHatchEssentiaOutput implements IActionHost, IGridProxyable {

    private AENetworkProxy gridProxy = null;
    private IMEMonitor<AEEssentiaStack> monitor = null;
    private final MachineSource asMachineSource = new MachineSource(this);
    private static Textures.BlockIcons.CustomIcon EHATCH_TX_ME;
    public long mTickTimer = 0;

    public MTEHatchEssentiaOutputME(int aID, String aBasicName, String aRegionalName) {
        super(aID, aBasicName, aRegionalName);
    }

    public MTEHatchEssentiaOutputME(String aBasicName) {
        super(aBasicName);
    }

    @Override
    public String[] getDescription() {
        String[] tooltip = new String[1];
        tooltip[0] = translateToLocal("gt.casing.no-mob-spawning");
        return tooltip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister blockIconRegister) {
        EHATCH_TX_ME = new Textures.BlockIcons.CustomIcon(GoodGenerator.MOD_ID + ":essentiaOutputHatch_ME");
        super.registerIcons(blockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        return new ITexture[] { TextureFactory.of(magicCasing), TextureFactory.of(EHATCH_TX_ME) };
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        getProxy().writeToNBT(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        getProxy().readFromNBT(aNBT);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEHatchEssentiaOutputME(mName);
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    void onChunkUnloadAE() {
        getProxy().onChunkUnload();
    }

    void invalidateAE() {
        getProxy().invalidate();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection forgeDirection) {
        return getProxy().getNode();
    }

    @Override
    public void gridChanged() {}

    @Override
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {}

    @Override
    public AENetworkProxy getProxy() {
        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", ItemRefer.Essentia_Output_Hatch_ME.get(1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }
        return this.gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(
            getBaseMetaTileEntity().getWorld(),
            getBaseMetaTileEntity().getXCoord(),
            getBaseMetaTileEntity().getYCoord(),
            getBaseMetaTileEntity().getZCoord());
    }

    @Override
    public IGridNode getActionableNode() {
        return getProxy().getNode();
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        return false;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, ForgeDirection side) {
        return this.addEssentia(aspect, amount, side, Actionable.MODULATE);
    }

    public int addEssentia(Aspect aspect, int amount, ForgeDirection side, Actionable mode) {
        long rejectedAmount = amount;
        if (this.getEssentiaMonitor()) {
            AEEssentiaStack rejected = this.monitor
                .injectItems(new AEEssentiaStack(aspect, amount), mode, this.getMachineSource());

            rejectedAmount = rejected != null ? rejected.getStackSize() : 0;
        }

        long acceptedAmount = (long) amount - rejectedAmount;
        return (int) acceptedAmount;
    }

    protected boolean getEssentiaMonitor() {
        this.monitor = null;
        IGridNode node = this.getProxy()
            .getNode();

        if (node != null) {
            try {
                this.monitor = (IMEMonitor<AEEssentiaStack>) this.getProxy()
                    .getStorage()
                    .getMEMonitor(ESSENTIA_STACK_TYPE);
            } catch (GridAccessException ignored) {}
        }
        return (this.monitor != null);
    }

    public MachineSource getMachineSource() {
        return this.asMachineSource;
    }
}
