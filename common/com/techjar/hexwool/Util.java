package com.techjar.hexwool;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.techjar.hexwool.api.IColorizable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.oredict.OreDictionary;

public class Util {
    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    public static boolean itemMatchesOre(ItemStack item, String ore) {
        for (ItemStack oreItem : OreDictionary.getOres(ore)) {
            if (OreDictionary.itemMatches(oreItem, item, false)) {
                return true;
            }
        }
        return false;
    }
    
    public static Object getItemOrBlockFromStack(ItemStack itemStack) {
        if (itemStack.itemID < 4096) {
            if (Block.blocksList[itemStack.itemID] != null && Block.blocksList[itemStack.itemID].blockID != 0) {
                return Block.blocksList[itemStack.itemID];
            }
        }
        if (Item.itemsList[itemStack.itemID] != null) {
            return Item.itemsList[itemStack.itemID];
        }
        return null;
    }
    
    public static boolean canColorizeItem(ItemStack itemStack) {
        if ((itemStack.itemID >= 298 && itemStack.itemID <= 301) || itemMatchesOre(itemStack, "blockWool")) return true;
        Object itemObj = getItemOrBlockFromStack(itemStack);
        return itemObj instanceof IColorizable;
    }
    
    public static boolean getItemHasColor(ItemStack itemStack) {
        itemStack = itemStack.copy();
        if (itemStack.itemID >= 298 && itemStack.itemID <= 301) {
            if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getCompoundTag("display").hasKey("color");
        }
        else if (itemStack.itemID == HexWool.idColoredWool) {
            if (itemStack.hasTagCompound()) return itemStack.getTagCompound().hasKey("color");
        }
        else {
            Object itemObj = getItemOrBlockFromStack(itemStack);
            if (itemObj instanceof IColorizable) {
                return ((IColorizable)itemObj).hasColor(itemStack);
            }
        }
        return false;
    }
    
    public static int getItemColor(ItemStack itemStack) {
        itemStack = itemStack.copy();
        if (itemStack.itemID >= 298 && itemStack.itemID <= 301) {
            if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getCompoundTag("display").getInteger("color");
        }
        else if (itemStack.itemID == HexWool.idColoredWool) {
            if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getInteger("color");
        }
        else {
            Object itemObj = getItemOrBlockFromStack(itemStack);
            if (itemObj instanceof IColorizable) {
                return ((IColorizable)itemObj).getColor(itemStack);
            }
        }
        return 0xFFFFFF;
    }
    
    public static ItemStack colorizeItem(ItemStack itemStack, int color) {
        itemStack = itemStack.copy();
        if (itemStack.itemID >= 298 && itemStack.itemID <= 301) {
            if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound("tag"));
            if (!itemStack.getTagCompound().hasKey("display")) itemStack.getTagCompound().setCompoundTag("display", new NBTTagCompound());
            itemStack.getTagCompound().getCompoundTag("display").setInteger("color", color);
        }
        if (itemMatchesOre(itemStack, "blockWool")) {
            if (itemStack.itemID != HexWool.idColoredWool) {
                itemStack.itemID = HexWool.idColoredWool;
                itemStack.setItemDamage(0);
            }
            if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound("tag"));
            itemStack.getTagCompound().setInteger("color", color);
        }
        Object itemObj = getItemOrBlockFromStack(itemStack);
        if (itemObj instanceof IColorizable) {
            itemStack = ((IColorizable)itemObj).colorize(itemStack, color);
        }
        return itemStack;
    }

    public static String colorToHex(int color) {
        StringBuilder hexBuilder = new StringBuilder(6);
        hexBuilder.setLength(6);
        for (int i = 5; i >= 0; i--) {
            int j = color & 0x0F;
            hexBuilder.setCharAt(i, hexDigits[j]);
            color >>= 4;
        }
        return hexBuilder.toString();
    }
    
    public static CMYKColor rgbToCmyk(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("Invalid RGB value: " + r + "," + g + "," + b);
        }
        
        if (r == 0 && g == 0 && b == 0) {
            return new CMYKColor(0, 0, 0, 1);
        }
        
        float c = 1 - (r / 255.0F);
        float m = 1 - (g / 255.0F);
        float y = 1 - (b / 255.0F);
        
        float k = Math.min(c, Math.min(m, y));
        c = (c - k) / (1 - k);
        m = (m - k) / (1 - k);
        y = (y - k) / (1 - k);
        
        return new CMYKColor(c, m, y, k);
    }
    
    public static RGBColor colorToRgb(int color) {
        return new RGBColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }
    
    public static int rgbToColor(int r, int g, int b) {
        return ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
    }
    
    public static int rgbaToColor(int r, int g, int b, int a) {
        return ((a << 24) & 0xFF000000) | ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
    }
    
    public static int rgbToColor(RGBColor rgb) {
        return rgbToColor(rgb.r, rgb.g, rgb.b);
    }
    
    public static CMYKColor rgbToCmyk(RGBColor rgb) {
        return rgbToCmyk(rgb.r, rgb.g, rgb.b);
    }
    
    public static CMYKColor colorToCmyk(int color) {
        return rgbToCmyk((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }
    
    public static class RGBColor {
        private int r, g, b;
        
        public RGBColor(int red, int green, int blue) {
            this.r = red;
            this.g = green;
            this.b = blue;
        }

        public int getRed() {
            return r;
        }

        public void setRed(int red) {
            this.r = red;
        }

        public int getGreen() {
            return g;
        }

        public void setGreen(int green) {
            this.g = green;
        }

        public int getBlue() {
            return b;
        }

        public void setBlue(int blue) {
            this.b = blue;
        }
    }
    
    public static class CMYKColor {
        private float c, m, y, k;
        
        public CMYKColor(float cyan, float magenta, float yellow, float black) {
            this.c = cyan;
            this.m = magenta;
            this.y = yellow;
            this.k = black;
        }

        public float getCyan() {
            return c;
        }

        public void setCyan(float cyan) {
            this.c = cyan;
        }

        public float getMagenta() {
            return m;
        }

        public void setMagenta(float magenta) {
            this.m = magenta;
        }

        public float getYellow() {
            return y;
        }

        public void setYellow(float yellow) {
            this.y = yellow;
        }

        public float getBlack() {
            return k;
        }

        public void setBlack(float black) {
            this.k = black;
        }
        
        public CMYKColor add(CMYKColor other) {
            return new CMYKColor(this.c + other.c, this.m + other.m, this.y + other.y, this.k + other.k);
        }
        
        public CMYKColor subtract(CMYKColor other) {
            return new CMYKColor(this.c - other.c, this.m - other.m, this.y - other.y, this.k - other.k);
        }
    }
}
