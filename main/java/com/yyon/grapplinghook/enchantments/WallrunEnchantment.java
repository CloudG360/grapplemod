package com.yyon.grapplinghook.enchantments;

import com.yyon.grapplinghook.GrappleConfig;
import com.yyon.grapplinghook.grapplemod;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class WallrunEnchantment extends Enchantment {
	public WallrunEnchantment() {
		super(grapplemod.getRarityFromInt(GrappleConfig.getconf().enchant_rarity_wallrun), EnchantmentType.ARMOR_FEET, new EquipmentSlotType[] {EquipmentSlotType.FEET});
	}
    public int getMinEnchantability(int enchantmentLevel)
    {
        return 1;
    }

    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + 40;
    }

    public int getMaxLevel()
    {
        return 1;
    }
}