package com.yyon.grapplinghook.items;

import com.yyon.grapplinghook.grapplemod;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;

/*
 * This file is part of GrappleMod.

    GrappleMod is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GrappleMod is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GrappleMod.  If not, see <http://www.gnu.org/licenses/>.
 */

public class launcherItem extends Item {
	
	public launcherItem() {
		super(new Item.Properties().stacksTo(1).tab(grapplemod.tabGrapplemod));
//		super();
//		maxStackSize = 1;
//		setFull3D();
//		setUnlocalizedName("launcheritem");
//		
//		this.setMaxDamage(500);
//		
//		setCreativeTab(grapplemod.tabGrapplemod);
		
//		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/*
	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		return 72000;
	}

	public void dorightclick(ItemStack stack, World worldIn, EntityPlayer player) {
		if (worldIn.isRemote) {
			grapplemod.proxy.launchplayer(player);
		}
	}
	
    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos blockpos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
    	ItemStack stack = playerIn.getHeldItem(hand);
        this.dorightclick(stack, worldIn, playerIn);
        
    	return EnumActionResult.SUCCESS;
	}
    
    @Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer entityLiving, EnumHand hand)
	{
    	ItemStack stack = entityLiving.getHeldItem(hand);
    	this.dorightclick(stack, worldIn, entityLiving);
        
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.NONE;
	}
    
	@Override
    @SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag par4)
	{
		list.add("Launches player");
		list.add("");
		list.add("Use crosshairs to aim");
		list.add(grapplemod.proxy.getkeyname(CommonProxyClass.keys.keyBindUseItem) + " - Launch player");
	}
	*/
}
