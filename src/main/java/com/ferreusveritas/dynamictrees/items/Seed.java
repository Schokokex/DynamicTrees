package com.ferreusveritas.dynamictrees.items;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;


public class Seed extends Item {

	private DynamicTree tree;//The tree this seed creates

	public Seed(String name) {
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	public void setTree(DynamicTree tree, ItemStack seedStack) {
		this.tree = tree;
	}
	
	public DynamicTree getTree(ItemStack seedStack) {
		return tree;
	}
	
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {

		if(entityItem.ticksExisted >= ConfigHandler.seedTimeToLive) {//1 minute by default(helps with lag)
			if(!entityItem.world.isRemote) {//Server side only
				BlockPos pos = new BlockPos(entityItem);
				if(entityItem.world.canBlockSeeSky(pos)) {
					Random rand = new Random();
					ItemStack seedStack = entityItem.getItem();
					int count = seedStack.getCount();
					while(count-- > 0) {
						if( rand.nextFloat() * (1f/ConfigHandler.seedPlantRate) <= getTree(seedStack).biomeSuitability(entityItem.world, pos) ){//1 in 16 chance if ideal
							if(plantSapling(entityItem.world, pos, seedStack)) {
								break;
							}
						}
					}
					entityItem.getItem().setCount(0);
				}
			}
			entityItem.setDead();
		}

		return false;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);

		//Handle Flower Pot interaction
		IBlockState blockState = world.getBlockState(pos);
		if(blockState.equals(Blocks.FLOWER_POT.getDefaultState())) { //Empty Flower Pot
			DynamicTree tree = getTree(heldItem);
			BlockBonsaiPot bonzaiPot = tree.getBonzaiPot();
			bonzaiPot.setTree(world, tree, pos);
			heldItem.shrink(1);
			return EnumActionResult.SUCCESS;
		}
		
		if (facing == EnumFacing.UP) {//Ensure this seed is only used on the top side of a block
			if (player.canPlayerEdit(pos, facing, heldItem) && player.canPlayerEdit(pos.up(), facing, heldItem)) {//Ensure permissions to edit block
				if(plantSapling(world, pos.up(), heldItem)) {//Do the planting
					heldItem.shrink(1);
					return EnumActionResult.SUCCESS;
				}
			}
		}

		return EnumActionResult.FAIL;
	}
	
	/**
	 * Checks surroundings and places a dynamic sapling block.
	 * 
	 * @param world
	 * @param pos
	 * @param seedStack
	 * @return
	 */
	public boolean plantSapling(World world, BlockPos pos, ItemStack seedStack) {
		DynamicTree tree = getTree(seedStack);
		
		if(world.getBlockState(pos).getBlock().isReplaceable(world, pos) && BlockDynamicSapling.canSaplingStay(world, tree, pos)) {
			world.setBlockState(pos, tree.getDynamicSapling());
			return true;
		}

		return false;
	}
	
}