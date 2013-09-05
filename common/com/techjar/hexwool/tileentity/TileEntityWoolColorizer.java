package com.techjar.hexwool.tileentity;

import com.techjar.hexwool.HexWool;
import com.techjar.hexwool.Util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityWoolColorizer extends TileEntity implements IInventory, ISidedInventory {
    public String colorCode = "";
    public int cyanDye;
    public int magentaDye;
    public int yellowDye;
    public int blackDye;
    private ItemStack[] inv;
    
    public TileEntityWoolColorizer() {
        inv = new ItemStack[6];
    }
    
    public void colorizeWool(int color) {
        ItemStack itemStack = inv[0];
        if (itemStack != null && Util.itemMatchesOre(itemStack, "blockWool")) {
            if (inv[1] != null) {
                ItemStack otherStack = inv[1];
                if (ItemStack.areItemStacksEqual(itemStack, inv[1])) {
                    
                }
            }
            if (itemStack.itemID != HexWool.idColoredWool) itemStack.itemID = HexWool.idColoredWool;
            itemStack.setItemDamage(0);
            if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound("tag"));
            itemStack.getTagCompound().setInteger("color", color);
            inv[1] = itemStack;
        }
    }

    @Override
    public int getSizeInventory() {
        return inv.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
            if (stack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                stack = stack.splitStack(amount);
                if (stack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
            setInventorySlotContents(slot, null);
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack) {
        inv[slot] = itemStack;
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInvName() {
        return "Wool Colorizer";
    }

    @Override
    public boolean isInvNameLocalized() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openChest() {
        // Useless
    }

    @Override
    public void closeChest() {
        // Useless
    }

    @Override
    public boolean isStackValidForSlot(int slot, ItemStack itemStack) {
        switch (slot) {
            case 0: return Util.itemMatchesOre(itemStack, "blockWool");
            case 1: return false;
            case 2: return Util.itemMatchesOre(itemStack, "dyeCyan");
            case 3: return Util.itemMatchesOre(itemStack, "dyeMagenta");
            case 4: return Util.itemMatchesOre(itemStack, "dyeYellow");
            case 5: return Util.itemMatchesOre(itemStack, "dyeBlack");
        }
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[]{ 0, 1, 2, 3, 4, 5 };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int side) {
        if (slot == 0 || (slot >= 2 && slot <= 5)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
        if (slot == 1) {
            return true;
        }
        return false;
    }
    
    @Override
    public void updateEntity() {
        super.updateEntity();
        checkDyes();
    }
    
    public void checkDyes() {
        if (cyanDye <= 750 && inv[2] != null && Util.itemMatchesOre(inv[2], "dyeCyan")) {
            cyanDye += 250;
            inv[2].stackSize--;
            if (inv[2].stackSize < 1) inv[2] = null;
        }
        if (magentaDye <= 750 && inv[3] != null && Util.itemMatchesOre(inv[3], "dyeMagenta")) {
            magentaDye += 250;
            inv[3].stackSize--;
            if (inv[3].stackSize < 1) inv[3] = null;
        }
        if (yellowDye <= 750 && inv[4] != null && Util.itemMatchesOre(inv[4], "dyeYellow")) {
            yellowDye += 250;
            inv[4].stackSize--;
            if (inv[4].stackSize < 1) inv[4] = null;
        }
        if (blackDye <= 750 && inv[5] != null && Util.itemMatchesOre(inv[5], "dyeBlack")) {
            blackDye += 250;
            inv[5].stackSize--;
            if (inv[5].stackSize < 1) inv[5] = null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        colorCode = tagCompound.getString("colorCode");
        cyanDye = tagCompound.getInteger("cyanDye");
        magentaDye = tagCompound.getInteger("magentaDye");
        yellowDye = tagCompound.getInteger("yellowDye");
        blackDye = tagCompound.getInteger("blackDye");
        
        NBTTagList tagList = tagCompound.getTagList("Inventory");
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = (NBTTagCompound)tagList.tagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inv.length) {
                inv[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setString("colorCode", colorCode);
        tagCompound.setInteger("cyanDye", cyanDye);
        tagCompound.setInteger("magentaDye", magentaDye);
        tagCompound.setInteger("yellowDye", yellowDye);
        tagCompound.setInteger("blackDye", blackDye);

        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inv.length; i++) {
            ItemStack stack = inv[i];
            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        tagCompound.setTag("Inventory", itemList);
    }
}
