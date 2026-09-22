package com.jkdr.abyssalascentdimensionpatcher.data;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

// FIX 1: Class name changed to 'dimensionRoofData' to match file name 'dimensionRoofData.java'
public class dimensionRoofData {
	// FIX 2: Changed 'int' to 'Integer' to allow null values
	private  Integer minHeight;
	private  List<ResourceKey<Block>> dimensionSpecificBlocks;

	public  List<ResourceKey<Item>> whitelistedTools;
	private  ServerLevel level;


	public dimensionRoofData(ServerLevel level, List<ResourceKey<Item>> whitelistedTools, List<ResourceKey<Block>> dimensionBlocks, @Nullable Integer minHeight) {
		this.level = level;
		this.whitelistedTools = whitelistedTools;
		// FIX 4: Corrected assignment from 'dimensionBlock' (which didn't exist) to 'dimensionBlocks'
		// FIX 5: Corrected target field from 'dimensionSpecificBlock' (which didn't exist) to 'dimensionSpecificBlocks'
		this.dimensionSpecificBlocks = dimensionBlocks;
		this.minHeight = minHeight;
	}

	public boolean ValidateAbyAscBlockBreak(BlockPos pos, ServerPlayer player) {
		//We check if the height is lower than the min height and if so return true as we dont handle this (not a target block)
		//Its best to have a min height as if it is not set this event will have to retrieve the block list everytime even if the player is near the bottom of the world
		if (minHeight != null) {
			//Min height exists and now we check
			if (pos.getY() < minHeight) {
				//Height is lower, we dont handle the check.
				return true;
			}
		}

		if (player != null) {
			if (player.isCreative()) {
				//If the player is in creative why should we let the player not mine the block?
				return true;
			}
		}

		Block block = level.getBlockState(pos).getBlock();
		RegistryAccess access = level.registryAccess();
		Optional<ResourceKey<Block>> blockKey =
			access.registryOrThrow(Registries.BLOCK).getResourceKey(block);

		//We check if the block the player wants to break does not match our list.
		if (!blockKey.isPresent() || !dimensionSpecificBlocks.contains(blockKey.get())) {
			//Does not match, we shouldnt handle this.
			return true;
		}

		//If there isnt a player set (from a block event etc then we shouldnt let mods break it)
		//Havent impliemented yet but still is a good check.
		if (player != null) {
			ItemStack heldStack = player.getMainHandItem();
			Item heldItem = heldStack.getItem();
			RegistryAccess registryAccess = player.level().registryAccess();
			Optional<ResourceKey<Item>> heldKey =
				registryAccess.registryOrThrow(Registries.ITEM).getResourceKey(heldItem);

			//We check if the player is currently holding one of the whitelisted tools
			if (!heldKey.isPresent() || whitelistedTools.contains(heldKey.get())) {
				//Either the tool doesnt exist at runtime or the player is holding the tool we return true.
				return true;
			}
		}

		//Player (or anything else) failed every check:
		// (X) Block was above the Y level
		// (X) Block was in the whitelisted
		// (X) Player was not holding the right tool.

		return false;
	}

}