package com.jkdr.abyssalascentdimensionpatcher.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

public class SpawnPosData extends SavedData {
    private static final String DATA_NAME = "abyssal_ascent_saved_pos"; // A unique ID for your data
    @Nullable
    private BlockPos spawnPos;

    public SpawnPosData() {
        this.spawnPos = null;
    }
    
    public SpawnPosData(BlockPos pos) {
        this.spawnPos = pos;
    }

    @Nullable
    public BlockPos getSpawnPos() {
        return this.spawnPos;
    }

    public void setSpawnPos(BlockPos pos) {
        this.spawnPos = pos;
        setDirty(); // Mark this data as needing to be saved
    }

    // This method is for loading the data from the world's NBT data
    public static SpawnPosData load(CompoundTag nbt) {
        SpawnPosData data = new SpawnPosData();
        if (nbt.contains("spawnPos")) {
            data.spawnPos = NbtUtils.readBlockPos(nbt.getCompound("spawnPos"));
        }
        return data;
    }

    // This method is for saving the data to NBT
    @Override
    public CompoundTag save(CompoundTag nbt) {
        if (this.spawnPos != null) {
            nbt.put("spawnPos", NbtUtils.writeBlockPos(this.spawnPos));
        }
        return nbt;
    }
    
    // A helper method to easily get an instance of this data for a given level
    public static SpawnPosData get(ServerLevel level) {
        // The first argument is the factory for creating a new instance if one doesn't exist
        // The second is the factory for loading an existing one from NBT
        // The third is the unique data name
        return level.getDataStorage().computeIfAbsent(SpawnPosData::load, SpawnPosData::new, DATA_NAME);
    }
}