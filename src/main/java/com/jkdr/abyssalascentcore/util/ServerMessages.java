package com.jkdr.abyssalascentcore.util;

import com.jkdr.abyssalascentcore.mining.MiningRules;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ported version which does not decode from the server but instead relies on both the client and the server having the translation key.
 */
public final class ServerMessages {
    private static final MutableComponent PREFIX = Component.translatable("message.abyssalascentcore.prefix").withStyle(ChatFormatting.GRAY);

    // Resolved lazily: sounds aren't registered when this class could first be loaded, and NetherEx may be absent.
    private static SoundEvent failSound;
    private static boolean failSoundResolved;

    private static final int CHAT_COOLDOWN_TICKS = 200;
    private static final Map<UUID, Long> LAST_WEAK_CHAT = new HashMap<>();

    private ServerMessages() {}

    /** Forgets a player's chat cooldown when they leave, so the map does not keep an entry for everyone who ever joined. */
    public static void forget(UUID player) {
        LAST_WEAK_CHAT.remove(player);
    }

    public static void welcome(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.abyssalascentcore.welcome"));
    }

    public static void discord(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.abyssalascentcore.discordnotice", PREFIX, link(ModInternalConfig.DISCORD_INVITE)));
    }

    public static void spawnStructureNew(ServerPlayer player, BlockPos pos) {
        player.sendSystemMessage(Component.translatable("message.abyssalascentcore.structurepos", PREFIX, pos.getX(), pos.getY(), pos.getZ()));
    }

    /**
     * Wrong pickaxe: steam and the obsidian-forming hiss at the block, then a chat message saying
     * which levels the held pickaxe can break this block in. Effects every attempt, chat at most every 10 seconds.
     */
    public static void pickaxeWeak(ServerPlayer player, ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D, y = pos.getY() + 0.5D, z = pos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.CLOUD, x, y + 0.3D, z, 12, 0.3D, 0.3D, 0.3D, 0.02D);
        level.playSound(null, x, y, z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

        // The effects above play on every attempt; the chat text is limited to once every 10 seconds per player.
        long now = level.getGameTime();
        if (now - LAST_WEAK_CHAT.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2) < CHAT_COOLDOWN_TICKS) return;
        LAST_WEAK_CHAT.put(player.getUUID(), now);

        player.sendSystemMessage(Component.translatable("message.abyssalascentcore.failedBlockBreak").withStyle(ChatFormatting.RED));

        // Which levels this pickaxe can break the block in. Nothing more is said if it can't break it anywhere.
        List<ResourceLocation> levels = LevelNames.ordered(MiningRules.levelsBreakable(level.getBlockState(pos), player.getMainHandItem()));
        if (!levels.isEmpty()) {
            MutableComponent list = Component.empty();
            for (int i = 0; i < levels.size(); i++) {
                if (i > 0) list.append(", ");
                list.append(LevelNames.of(levels.get(i)));
            }
            player.sendSystemMessage(Component.translatable("message.abyssalascentcore.canBreakIn", list.withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
        }
    }

    /** Non player break (explosion, other mod): sound only. */
    public static void invalidSourceMine(Level level, BlockPos pos) {
        playFailSound(level, pos);
    }

    private static void playFailSound(Level level, BlockPos pos) {
        if (!failSoundResolved) {
            failSound = ForgeRegistries.SOUND_EVENTS.getValue(ModInternalConfig.blockBreakFailSoundLocation);
            failSoundResolved = true;
        }
        if (failSound != null) {
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, failSound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static MutableComponent link(String url) {
        return Component.literal(url).withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withColor(ChatFormatting.BLUE)
                .withUnderlined(true));
    }
}
