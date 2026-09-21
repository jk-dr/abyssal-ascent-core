package com.jkdr.abyssalascentcore.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

/** Lets the player move the depth meter around the screen. */
public class DepthMeterConfigScreen extends Screen {
    private final Screen parent;
    private LabelledEditBox xBox;
    private LabelledEditBox yBox;

    public DepthMeterConfigScreen(Screen parent) {
        super(Component.translatable("screen.abyssalascentcore.depth_meter"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int top = this.height / 2 - 10 - 135;

        Component xLabel = Component.translatable("screen.abyssalascentcore.depth_meter.x");
        this.xBox = this.addRenderableWidget(new LabelledEditBox(this.font,
                this.width / 2 - 50 - this.font.width(xLabel), top, 100, 20, xLabel,
                String.valueOf(DepthMeterConfig.OVERLAY_X.get())));

        Component yLabel = Component.translatable("screen.abyssalascentcore.depth_meter.y");
        this.yBox = this.addRenderableWidget(new LabelledEditBox(this.font,
                this.width / 2 - 50 - this.font.width(yLabel), top + 30, 100, 20, yLabel,
                String.valueOf(DepthMeterConfig.OVERLAY_Y.get())));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .pos(this.width / 2 - 100, top + 50).size(200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, DepthMeter.COLOUR);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        save(DepthMeterConfig.OVERLAY_X, this.xBox);
        save(DepthMeterConfig.OVERLAY_Y, this.yBox);

        if (this.minecraft != null && this.parent != null) {
            this.minecraft.setScreen(this.parent);
            return;
        }
        super.onClose();
    }

    private static void save(net.minecraftforge.common.ForgeConfigSpec.IntValue value, EditBox box) {
        try {
            value.set(Integer.parseInt(box.getValue().trim()));
        } catch (NumberFormatException ignored) {
            // Keep the previous value if the box was left empty or holds nonsense.
        }
    }

    /** An edit box that only accepts digits and draws its label to its left. */
    private static class LabelledEditBox extends EditBox {
        private final Component label;
        private final Font font;

        LabelledEditBox(Font font, int x, int y, int width, int height, Component label, String value) {
            super(font, x, y, width, height, label);
            this.label = label;
            this.font = font;
            this.setFilter(DIGITS);
            this.setValue(value);
        }

        private static final Predicate<String> DIGITS = text -> text.chars().allMatch(Character::isDigit);

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int labelWidth = this.font.width(this.label);
            graphics.drawString(this.font, this.label, this.getX(), this.getY() + this.height / 2 - this.font.lineHeight / 2, DepthMeter.COLOUR);
            this.setX(this.getX() + labelWidth + 2);
            super.render(graphics, mouseX, mouseY, partialTick);
            this.setX(this.getX() - labelWidth - 2);
        }
    }
}
