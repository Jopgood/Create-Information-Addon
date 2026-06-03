package com.jopgood.cfwinfo.client.gui;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.config.CommonConfig.OverlayPosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * In-game editor for freely positioning the overlay by dragging it.
 *
 * <p>The preview is drawn by the live {@link TankSpriteOverlay#renderCompositeAt} method at the same
 * anchor the HUD would use, so it is WYSIWYG — what you drop is exactly what renders. Dragging keeps
 * the cursor's offset within the sprite (a "grab offset"), so picking the sprite up anywhere doesn't
 * snap it under the cursor, and the saved anchor matches the previewed one precisely.
 */
public class OverlayEditScreen extends Screen {

    private static final Component INSTRUCTION =
            Component.literal("Drag the overlay to position it, then Save. Or pick a preset.");

    private final TankSpriteOverlay previewOverlay = new TankSpriteOverlay();

    // Working anchor (top-left of the tank sprite) in GUI pixels.
    private int anchorX;
    private int anchorY;

    // Drag state.
    private boolean dragging = false;
    private int grabOffsetX;
    private int grabOffsetY;

    public OverlayEditScreen() {
        super(Component.literal("Overlay Position"));
    }

    @Override
    protected void init() {
        // Seed the working anchor from where the overlay currently renders, so it doesn't jump.
        int[] anchor = TankSpriteOverlay.currentAnchor(this.width, this.height);
        anchorX = anchor[0];
        anchorY = anchor[1];
        clampAnchor();

        int cx = this.width / 2;
        int bottom = this.height - 28;

        // Preset reset buttons (row above the action row).
        int presetY = bottom - 24;
        int presetW = 78;
        int gap = 4;
        int rowWidth = presetW * 4 + gap * 3;
        int startX = cx - rowWidth / 2;
        addRenderableWidget(Button.builder(Component.literal("Top Left"), b -> applyPreset(OverlayPosition.TOP_LEFT))
                .bounds(startX, presetY, presetW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Top Right"), b -> applyPreset(OverlayPosition.TOP_RIGHT))
                .bounds(startX + (presetW + gap), presetY, presetW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Bottom Left"), b -> applyPreset(OverlayPosition.BOTTOM_LEFT))
                .bounds(startX + 2 * (presetW + gap), presetY, presetW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Bottom Right"), b -> applyPreset(OverlayPosition.BOTTOM_RIGHT))
                .bounds(startX + 3 * (presetW + gap), presetY, presetW, 20).build());

        // Action row: Save / Cancel.
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> save())
                .bounds(cx - 104, bottom, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
                .bounds(cx + 4, bottom, 100, 20).build());
    }

    /** Switches to a preset position and stores it immediately, then closes. */
    private void applyPreset(OverlayPosition position) {
        CommonConfig.setOverlayPosition(position);
        onClose();
    }

    /** Persists the dragged position as a CUSTOM anchor and closes. */
    private void save() {
        CommonConfig.setCustomPosition(anchorX, anchorY);
        onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Light dim so the UI is readable but the world (and where the overlay sits on it) stays visible.
        renderBackground(graphics, mouseX, mouseY, partialTick);

        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player != null) {
            int w = TankSpriteOverlay.compositeWidthPx(player);
            int h = TankSpriteOverlay.compositeHeightPx();

            // Highlight the draggable region.
            int border = dragging ? 0xFFFFE066 : 0x80FFFFFF;
            graphics.renderOutline(anchorX - 1, anchorY - 1, w + 2, h + 2, border);

            // Live preview using the exact HUD rendering path (CUSTOM layout: items below, no padding).
            previewOverlay.renderCompositeAt(graphics, anchorX, anchorY, player, OverlayPosition.CUSTOM);
        }

        // Title + instructions.
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, INSTRUCTION, this.width / 2, 22, 0xFFB0B0B0);

        // Buttons on top of everything.
        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    /** Background: a light dim rather than the default blur, so the world is visible while editing. */
    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x40000000);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.minecraft != null && this.minecraft.player != null && isInsideOverlay(mouseX, mouseY)) {
            dragging = true;
            grabOffsetX = (int) Math.round(mouseX) - anchorX;
            grabOffsetY = (int) Math.round(mouseY) - anchorY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            anchorX = (int) Math.round(mouseX) - grabOffsetX;
            anchorY = (int) Math.round(mouseY) - grabOffsetY;
            clampAnchor();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInsideOverlay(double mouseX, double mouseY) {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return false;
        int w = TankSpriteOverlay.compositeWidthPx(player);
        int h = TankSpriteOverlay.compositeHeightPx();
        return mouseX >= anchorX && mouseX <= anchorX + w && mouseY >= anchorY && mouseY <= anchorY + h;
    }

    /** Keeps the composite fully on screen. */
    private void clampAnchor() {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        int w = player != null ? TankSpriteOverlay.compositeWidthPx(player) : 0;
        int h = TankSpriteOverlay.compositeHeightPx();
        anchorX = OverlayAnchor.clamp(anchorX, 0, Math.max(0, this.width - w));
        anchorY = OverlayAnchor.clamp(anchorY, 0, Math.max(0, this.height - h));
    }

    @Override
    public boolean isPauseScreen() {
        // Keep the world rendering so the player can see the overlay in context.
        return false;
    }
}
