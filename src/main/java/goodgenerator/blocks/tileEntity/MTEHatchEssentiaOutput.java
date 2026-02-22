package goodgenerator.blocks.tileEntity;

import static goodgenerator.loader.Loaders.magicCasing;
import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.Map;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import goodgenerator.main.GoodGenerator;
import gregtech.api.enums.HarvestTool;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;

public class MTEHatchEssentiaOutput extends MetaTileEntity implements IAspectContainer, IEssentiaTransport {

    public static final int CAPACITY = 256;
    private static Textures.BlockIcons.CustomIcon EHATCH_TX;
    protected AspectList mAspects = new AspectList();

    public MTEHatchEssentiaOutput(int aID, String aBasicName, String aRegionalName) {
        super(aID, aBasicName, aRegionalName, 0);
    }

    public MTEHatchEssentiaOutput(String aBasicName) {
        super(aBasicName, 0);
    }

    @Override
    public String[] getDescription() {
        String[] tooltip = new String[3];
        tooltip[0] = translateToLocal("gt.casing.no-mob-spawning");
        tooltip[1] = translateToLocal("EssentiaOutputHatch.tooltip.0");
        tooltip[2] = translateToLocal("EssentiaOutputHatch.tooltip.1") + " " + MTEHatchEssentiaOutput.CAPACITY;
        return tooltip;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister blockIconRegister) {
        EHATCH_TX = new Textures.BlockIcons.CustomIcon(GoodGenerator.MOD_ID + ":essentiaOutputHatch");
        super.registerIcons(blockIconRegister);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        return new ITexture[] { TextureFactory.of(magicCasing), TextureFactory.of(EHATCH_TX) };
    }

    // TODO
    // @Override
    // public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
    // ItemStack tItemStack = aPlayer.getHeldItem();
    // if (tItemStack == null) {
    // clear();
    // GTUtility.sendChatTrans(aPlayer, "essentiaoutputhatch.chat.0");
    // }
    // return true;
    // }

    @Override
    public void saveNBTData(NBTTagCompound nbttagcompound) {
        Aspect[] aspectA = this.mAspects.getAspects();
        NBTTagList nbtTagList = new NBTTagList();
        for (Aspect aspect : aspectA) {
            if (aspect != null) {
                NBTTagCompound f = new NBTTagCompound();
                f.setString("key", aspect.getTag());
                f.setInteger("amount", this.mAspects.getAmount(aspect));
                nbtTagList.appendTag(f);
            }
        }
        nbttagcompound.setTag("Aspects", nbtTagList);
    }

    @Override
    public void loadNBTData(NBTTagCompound nbttagcompound) {
        this.mAspects.aspects.clear();
        NBTTagList tlist = nbttagcompound.getTagList("Aspects", 10);
        for (int j = 0; j < tlist.tagCount(); ++j) {
            NBTTagCompound rs = tlist.getCompoundTagAt(j);
            if (rs.hasKey("key")) mAspects.add(Aspect.getAspect(rs.getString("key")), rs.getInteger("amount"));
        }
    }

    @Override
    public byte getTileEntityBaseType() {
        return HarvestTool.WrenchLevel2.toTileEntityBaseType();
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEHatchEssentiaOutput(mName);
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    public void clear() {
        this.mAspects.aspects.clear();
    }

    private int remainingCapacity() {
        return CAPACITY - this.getEssentiaAmount(null);
    }

    @Override
    public AspectList getAspects() {
        return this.mAspects;
    }

    @Override
    public void setAspects(AspectList aspectList) {
        for (Map.Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
            this.addEssentia(entry.getKey(), entry.getValue(), null);
        }
    }

    @Override
    public boolean doesContainerAccept(Aspect var1) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        int remaining = 0;
        if (amount > this.remainingCapacity()) {
            remaining = amount - this.remainingCapacity();
            this.mAspects.add(aspect, this.remainingCapacity());
        } else this.mAspects.add(aspect, amount);
        this.markDirty();
        return remaining;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (this.mAspects != null && this.mAspects.getAmount(aspect) >= amount) {
            this.mAspects.remove(aspect, amount);
            this.markDirty();
            return true;
        } else return false;

    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        return true;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return this.mAspects.getAmount(aspect) >= amount;
    }

    @Override
    public boolean doesContainerContain(AspectList aspectList) {
        for (Map.Entry<Aspect, Integer> entry : aspectList.aspects.entrySet()) {
            if (this.mAspects.getAmount(entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return this.mAspects.getAmount(aspect);
    }

    @Override
    public boolean isConnectable(ForgeDirection var1) {
        return true;
    }

    @Override
    public boolean canInputFrom(ForgeDirection var1) {
        return false;
    }

    @Override
    public boolean canOutputTo(ForgeDirection var1) {
        return true;
    }

    @Override
    public void setSuction(Aspect var1, int var2) {}

    @Override
    public Aspect getSuctionType(ForgeDirection var1) {
        return null;
    }

    @Override
    public int getSuctionAmount(ForgeDirection var1) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection var3) {
        return this.takeFromContainer(aspect, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, ForgeDirection direction) {
        return amount - addToContainer(aspect, amount);
    }

    @Override
    public Aspect getEssentiaType(ForgeDirection var1) {
        return this.mAspects.size() > 0 ? this.mAspects.getAspects()[0] : null;
    }

    @Override
    public int getEssentiaAmount(ForgeDirection var1) {
        return this.mAspects.visSize();
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return true;
    }
}
