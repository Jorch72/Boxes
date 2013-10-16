package ml.boxes.recipe;

import java.util.Arrays;

import ml.boxes.Boxes;
import ml.boxes.Registry;
import ml.boxes.api.safe.IItemMech;
import ml.boxes.block.MetaType;
import ml.boxes.tile.safe.MechRegistry;
import ml.core.item.ItemUtils;
import ml.core.item.StackUtils;
import ml.core.item.recipe.RecipeShapedVariable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeSafe extends RecipeShapedVariable {

//	@Override
//	public boolean matches(InventoryCrafting inv, World world) {
//
//		boolean steelOn = Arrays.asList(OreDictionary.getOreNames()).contains("ingotSteel");
//		for (int i : new int[]{0,1,2, 3, 6,7,8}){
//			if (Boxes.config.lockbox_useSteel && steelOn) {
//				if (OreDictionary.getOreID(inv.getStackInSlot(i)) != OreDictionary.getOreID("ingotSteel"))
//					return false;
//			} else {
//				if (!ItemUtils.checkItemEquals(Item.ingotIron, inv.getStackInSlot(i)))
//					return false;
//			}
//		}
//		
//		return true;
//	}
	
	public RecipeSafe() {
		super(3,3);
	}

	@Override
	public boolean itemMatchesAt(int lx, int ly, ItemStack is) {
	
		boolean steelOn = Arrays.asList(OreDictionary.getOreNames()).contains("ingotSteel");
		if (lx==2 && ly==1) {
			if (is==null || !(is.getItem() instanceof IItemMech)) return false;
			String mId = ((IItemMech)is.getItem()).getMechID(is);
			return MechRegistry.isIdRegistered(mId);
		} else if ((ly==0 || ly==2) || lx==0) {
			if (Boxes.config.lockbox_useSteel && steelOn) {
				return (OreDictionary.getOreID(is) == OreDictionary.getOreID("ingotSteel"));
			} else {
				return ItemUtils.checkItemEquals(Item.ingotIron, is);
			}
		}
		return super.itemMatchesAt(lx, ly, is);
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
		ItemStack mechStack = inventorycrafting.getStackInRowAndColumn(2, 1);
		ItemStack safeStack = new ItemStack(Registry.BlockMeta, 1, MetaType.Safe.meta);
		
		NBTTagCompound mechTag = StackUtils.getStackTag(mechStack);
		NBTTagCompound safeTag = StackUtils.getStackTag(safeStack);
		
		safeTag.setString("mech_id", ((IItemMech)mechStack.getItem()).getMechID(mechStack));
		safeTag.setCompoundTag("mech_data", mechTag);
		
		return safeStack;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

}