package ml.boxes;

import buildcraft.api.inventory.ISpecialInventory;
import ml.boxes.data.BoxData;
import ml.boxes.network.packets.PacketUpdateData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityBox extends TileEntity implements IInventory, IBox, ISpecialInventory {

	public float prevAngleOuter = 0F; //Used for smoothness when FPS > 1 tick
	public float flapAngleOuter = 0F;
	
	public float prevAngleInner = 0F; //Used for smoothness when FPS > 1 tick
	public float flapAngleInner = 0F;
	
	public int facing = 0;
	private int syncTime = 0;
	private int users = 0;
	
	private BoxData data = new BoxData(this);
	
	public TileEntityBox() {
		
	}
	
	@Override
	public void updateEntity() {
		
		super.updateEntity();
		if ((++syncTime % 20) == 0)
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, Boxes.config.boxBlockID, 2, facing);
		
		prevAngleOuter = flapAngleOuter;
		prevAngleInner = flapAngleInner;
		float fc = 0.1F;
		if (users > 0 && flapAngleOuter == 0){
			// TODO Sound
		}
		if (users == 0 && flapAngleOuter > 0 || users > 0 && flapAngleInner < 1F){
						
			if (users>0){
				flapAngleOuter += fc;
				if (flapAngleOuter>0.5)
					flapAngleInner += fc;
			} else {
				flapAngleInner -= fc;
				if (flapAngleInner<0.5)
					flapAngleOuter -= fc;
			}
			
			if (flapAngleOuter>1.0F)
				flapAngleOuter = 1.0F;
			if (flapAngleInner>1.0F)
				flapAngleInner = 1.0F;
			
			if (flapAngleOuter < 0.5 && prevAngleOuter >= 0.5){
				// TODO Sound
			}
			
			if (flapAngleOuter<0F)
				flapAngleOuter = 0F;
			if (flapAngleInner<0F)
				flapAngleInner = 0F;
		}
	}

	@Override
	public boolean receiveClientEvent(int par1, int par2) {
		switch (par1) {
		case 1:
			users = par2;
			break;
		case 2:
			facing = par2;
			break;
		}
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		data=new BoxData(par1nbtTagCompound.getCompoundTag("box"), this);
		facing=par1nbtTagCompound.getInteger("faceDir");
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setCompoundTag("box", data.asNBTTag());
		par1nbtTagCompound.setInteger("faceDir", facing);
	}

	public void setFacing(int f){
		facing = f;
	}
	
	@Override
	public int getSizeInventory() {
		return data.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return data.getStackInSlot(var1);
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		return data.decrStackSize(var1, var2);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return data.getStackInSlotOnClosing(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		data.setInventorySlotContents(var1, var2);
	}

	@Override
	public String getInvName() {
		return data.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return data.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return data.isUseableByPlayer(var1);
	}

	@Override
	public void openChest() {
		users++;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, Boxes.config.boxBlockID, 1, users);
	}

	@Override
	public void closeChest() {
		users--;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, Boxes.config.boxBlockID, 1, users);
	}

	@Override
	public void saveData() {}

	@Override
	public void boxOpen() {
		openChest();
	}

	@Override
	public void boxClose() {
		closeChest();
	}

	@Override
	public BoxData getBoxData() {
		return data;
	}

	@Override
	public Packet getDescriptionPacket() {
		return (new PacketUpdateData(this)).convertToPkt250();
	}

	@Override
	public void ejectItem(ItemStack is) {
		if (!worldObj.isRemote){
			EntityItem ei = new EntityItem(worldObj, 0.5F + xCoord, 1F + yCoord, 0.5F + zCoord, is);
			worldObj.spawnEntityInWorld(ei);
		}
	}

	@Override
	public int addItem(ItemStack stack, boolean doAdd, ForgeDirection from) {
		if (!getBoxData().ISAllowedInBox(stack)){
			doAdd = false;
			return 0;
		}
		
		return getBoxData().pipeTransferIn(stack, doAdd, from);
	}

	@Override
	public ItemStack[] extractItem(boolean doRemove, ForgeDirection from,
			int maxItemCount) {

		return getBoxData().pipeExtract(doRemove, from, maxItemCount);
	}

	@Override
	public boolean func_94042_c() { //Inventory Named
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean func_94041_b(int i, ItemStack itemstack) { //Stack Allowed (YES!)
		// TODO Auto-generated method stub
		return false;
	}
}
