package com.jkdr.abyssalascentcore.client;

import com.jkdr.abyssalascentcore.AbyssalAscentCore;
import com.jkdr.abyssalascentcore.depth.Discovery;
import com.jkdr.abyssalascentcore.depth.DimensionStack;
import com.jkdr.abyssalascentcore.depth.Locator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A vertical bar on the far right of the screen that covers the whole dimension stack.
 * Every player the server lets this player see has a dot in their own colour.
 * The player's own dot is white to reduce confusion
 */
public final class DepthBar {
    private static final ResourceLocation BAR = new ResourceLocation(AbyssalAscentCore.MODID, "textures/gui/player_bar.png");
    private static final ResourceLocation DOT = new ResourceLocation(AbyssalAscentCore.MODID, "textures/gui/player_dot.png");
    private static final ResourceLocation UNDISCOVERED = new ResourceLocation(AbyssalAscentCore.MODID, "textures/gui/player_bar_undiscovered.png");

    private static final int BAR_WIDTH = 8;
    private static final int BAR_HEIGHT = 128;
    private static final int DOT_WIDTH = 8;
    private static final int DOT_HEIGHT = 3;
    /** The bar artwork covers these rows of its texture; the dot travels between them. */
    private static final int TRACK_TOP = 4;
    private static final int TRACK_BOTTOM = 124;
    /** The artwork is one band per dimension, top to bottom, split by a divider row every 15 rows starting at TRACK_TOP. */
    private static final int BANDS = 8;
    private static final int BAND_STRIDE = 15;
    private static final int MARGIN = 2;
    private static final int OWN_COLOUR = 0xFFFFFF;

    private DepthBar() {}

    /** A dot's height, and the name to show for it (null for the player's own dot, which never has one). */
    private record Member(float centre, String name) {}

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        float progress = DimensionStack.progress(player.level().dimension(), player.getY());
        if (progress < 0.0F) return;

        // Shrink on very small GUI sizes so the whole bar stays on screen.
        float scale = Math.min(1.0F, (screenHeight - 2.0F * MARGIN) / BAR_HEIGHT);
        float x = screenWidth - MARGIN - BAR_WIDTH * scale;
        float y = (screenHeight - BAR_HEIGHT * scale) / 2.0F;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);

        graphics.blit(BAR, 0, 0, 0.0F, 0.0F, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);

        // Dimensions this player has not been in yet are covered up. The one they are in now always counts as entered,
        // in case the server's record has not reached the client yet.
        ResourceLocation ownDimension = player.level().dimension().location();
        drawUndiscovered(graphics, ownDimension);

        // Names are shown while Tab is held, if the server allows it.
        boolean names = Locator.namesEnabled() && minecraft.options.keyPlayerList.isDown();
        List<Member> members = new ArrayList<>();

        for (Locator.Entry other : Locator.players()) {
            // Nobody in a dimension that is still hidden is shown, so a dot cannot give it away.
            if (!other.dimension().equals(ownDimension) && !Discovery.isDiscovered(other.dimension())) continue;
            float centre = dotCentre(other.dimension(), other.y());
            if (Float.isNaN(centre)) continue;
            drawDot(graphics, centre, other.colour());
            members.add(new Member(centre, names && !other.name().isEmpty() ? other.name() : null));
        }

        // The player's own dot goes last so it is on top of everyone else's. It has no label of its own,
        // but it still counts towards the "+N" of any group it is close to.
        float own = dotCentre(player.level().dimension().location(), player.getY());
        if (!Float.isNaN(own)) {
            drawDot(graphics, own, OWN_COLOUR);
            members.add(new Member(own, null));
        }

        if (names) drawLabels(graphics, minecraft.font, members);

        graphics.pose().popPose();
    }

    /**
     * Draws the "?" over the band of every dimension of the stack the player has not entered.
     * We don't really care if the player sees this by accident though, dimensions aren't listed by name.
     * */
    private static void drawUndiscovered(GuiGraphics graphics, ResourceLocation ownDimension) {
        List<DimensionStack.Entry> stack = DimensionStack.clientStack();
        // The bands only line up with the artwork when the stack has exactly as many dimensions as the bar has bands.
        if (stack.size() != BANDS) return;

        for (int i = 0; i < BANDS; i++) {
            ResourceLocation dimension = stack.get(i).dimension();
            if (dimension.equals(ownDimension) || Discovery.isDiscovered(dimension)) continue;

            int top = TRACK_TOP + i * BAND_STRIDE + 1;
            int height = BAND_STRIDE - 1;
            graphics.blit(UNDISCOVERED, 0, top, 0.0F, (float) top, BAR_WIDTH, height, BAR_WIDTH, BAR_HEIGHT);
        }
    }

    /** Where the middle of a dot goes on the bar for a position in a dimension, or NaN when the dimension is not in the stack. */
    private static float dotCentre(ResourceLocation dimension, double y) {
        DimensionStack.Position position = DimensionStack.position(dimension, y);
        if (position == null) return Float.NaN;

        // The dimensions have different heights but the bar gives each the same band, so the dot is placed inside the
        // band of its dimension. Without a matching band count it falls back to the 0-1 value over the whole bar.
        if (position.count() == BANDS) {
            float bandTop = TRACK_TOP + position.index() * BAND_STRIDE + 1;
            return bandTop + position.fractionDown() * (BAND_STRIDE - 2);
        }
        return TRACK_TOP + (1.0F - DimensionStack.progress(dimension, y)) * (TRACK_BOTTOM - TRACK_TOP);
    }

    private static void drawDot(GuiGraphics graphics, float centre, int rgb) {
        // Keep the whole dot inside the artwork.
        int dotY = Math.max(TRACK_TOP, Math.min(TRACK_BOTTOM - DOT_HEIGHT + 1, Math.round(centre - DOT_HEIGHT / 2.0F)));
        graphics.setColor((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, 1.0F);
        graphics.blit(DOT, 0, dotY, 0.0F, 0.0F, DOT_WIDTH, DOT_HEIGHT, DOT_WIDTH, DOT_HEIGHT);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws a name to the left of the dots. Dots too close together to have a line each form a group.
     * To ensure the UI doesn't get cluttered: the first name, then "+N" for everyone else in the group
     * The player's own dot included since the white dot is enough to signal and is always on top.
     */
    private static void drawLabels(GuiGraphics graphics, Font font, List<Member> members) {
        if (members.isEmpty()) return;
        members.sort(Comparator.comparing(Member::centre));

        int lineHeight = font.lineHeight;
        int start = 0;
        while (start < members.size()) {
            int end = start + 1;
            while (end < members.size() && members.get(end).centre() - members.get(end - 1).centre() < lineHeight) end++;

            float average = 0.0F;
            String name = null;
            for (int i = start; i < end; i++) {
                average += members.get(i).centre();
                if (name == null) name = members.get(i).name();
            }
            average /= end - start;

            if (name != null) {
                int others = end - start - 1;
                String text = others > 0 ? name + " +" + others : name;
                graphics.drawString(font, text, -2 - font.width(text), Math.round(average - lineHeight / 2.0F), 0xFFFFFF);
            }
            start = end;
        }
    }
}
