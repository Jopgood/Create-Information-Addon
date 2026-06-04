package com.jopgood.cfwinfo.client.gui;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.config.CommonConfig.OverlayPosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * In-game editor for positioning and sizing the overlay.
 *
 * <p>The preview is drawn by {@link TankSpriteOverlay#renderCompositeAt} at the anchor the HUD would
 * use, so it matches the live overlay. Dragging preserves the cursor's offset within the sprite, so
 * the sprite is not snapped under the cursor when picked up.
 *
 * <p>The scale slider previews live in memory only; the value is persisted when the editor commits
 * (Save or a preset) and reverted if cancelled.
 */
public class OverlayEditScreen extends Screen {

    private static final Component INSTRUCTION =
            Component.literal("Drag the overlay or pick a preset to preview, then Save.");

    private static final double MIN_SCALE = 0.5;
    private static final double MAX_SCALE = 5.0;

    private final TankSpriteOverlay previewOverlay = new TankSpriteOverlay();
    private final TankTooltipOverlay previewTooltip = new TankTooltipOverlay();

    // True when editing the detailed (text) overlay; false for the simplified sprite. The position
    // config is shared, but the preview, sizing and controls follow whichever mode is active.
    private boolean detailed;

    // What Save will persist: a preset enum, or CUSTOM once the overlay has been dragged.
    private OverlayPosition pendingPosition = OverlayPosition.TOP_LEFT;

    // Working anchor (top-left of the composite) in GUI pixels.
    private int anchorX;
    private int anchorY;

    // Drag state.
    private boolean dragging = false;
    private int grabOffsetX;
    private int grabOffsetY;

    // Scale preview/commit state.
    private double originalScale;
    private boolean committed = false;

    public OverlayEditScreen() {
        super(Component.literal("Overlay Position"));
    }

    @Override
    protected void init() {
        // Edit whichever overlay is currently active; the position config is shared between them.
        detailed = !CommonConfig.isSimplifiedEnabled();

        // Remember the scale we opened with so Cancel/Escape can revert a live preview.
        originalScale = CommonConfig.getSpriteScaleFactor();
        committed = false;
        pendingPosition = CommonConfig.getOverlayPosition();

        // Seed the working anchor from where the overlay currently renders, so it doesn't jump.
        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player != null) {
            int[] anchor = currentAnchor(player);
            anchorX = anchor[0];
            anchorY = anchor[1];
        }
        clampAnchor();

        int cx = this.width / 2;
        int bottom = this.height - 28;
        int presetY = bottom - 24;
        int sliderY = presetY - 24;

        // Scale slider only applies to the sprite; the detailed text view does not use it.
        if (!detailed) {
            ScaleSlider slider = new ScaleSlider(cx - 110, sliderY, 220, 20, originalScale);
            slider.setTooltip(Tooltip.create(Component.literal(
                    "Adjusts the tank sprite size. The overlay already scales with Minecraft's "
                            + "GUI Scale setting; this is an extra multiplier on top of that.")));
            addRenderableWidget(slider);
        }

        // Preset reset buttons.
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

    /**
     * Previews a preset position without saving or closing, so the player can see it before
     * committing. The preview moves to exactly where the live HUD would render that preset.
     */
    private void applyPreset(OverlayPosition position) {
        pendingPosition = position;
        Player player = this.minecraft != null ? this.minecraft.player : null;
        int w = player != null ? contentW(player) : 0;
        int h = player != null ? contentH(player) : 0;
        int[] anchor = OverlayAnchor.resolve(position, this.width, this.height, w, h);
        anchorX = anchor[0];
        anchorY = anchor[1];
        clampAnchor();
    }

    /** Persists the previewed position (a preset, or CUSTOM if dragged) plus any scale change. */
    private void save() {
        committed = true;
        if (pendingPosition == OverlayPosition.CUSTOM) {
            CommonConfig.setCustomPosition(anchorX, anchorY); // saves, flushing the previewed scale too
        } else {
            CommonConfig.setOverlayPosition(pendingPosition); // saves, flushing the previewed scale too
        }
        onClose();
    }

    private static final Component DRAG_HINT = Component.literal("Release to place the overlay.");

    /** Low item-icon z so a resting preview stays behind the controls (during a drag they're hidden). */
    private static final int PREVIEW_ITEM_Z = 0;

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Light dim so the UI is readable but the world (and where the overlay sits on it) stays visible.
        renderBackground(graphics);

        // The overlay preview.
        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player != null) {
            int w = contentW(player);
            int h = contentH(player);

            int border = dragging ? 0xFFFFE066 : 0x80FFFFFF;
            graphics.renderOutline(anchorX - 1, anchorY - 1, w + 2, h + 2, border);

            if (detailed) {
                previewTooltip.renderPreviewAt(graphics, anchorX, anchorY, player);
            } else {
                previewOverlay.renderCompositeAt(graphics, anchorX, anchorY, player, PREVIEW_ITEM_Z);
            }
        }

        if (dragging) {
            // Hide the controls while dragging so the preview is never obscured by (or overlapping) them.
            graphics.drawCenteredString(this.font, DRAG_HINT, this.width / 2, 8, 0xFFFFFFFF);
        } else {
            graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
            graphics.drawCenteredString(this.font, INSTRUCTION, this.width / 2, 22, 0xFFB0B0B0);
            for (Renderable renderable : this.renderables) {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    /** Background: a light dim rather than the default blur, so the world is visible while editing. */
    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0x40000000);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Controls render on top, so let them handle the click before starting a drag.
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && this.minecraft != null && this.minecraft.player != null && isInsideOverlay(mouseX, mouseY)) {
            dragging = true;
            // Dragging means the player is choosing a free position; Save will store it as CUSTOM.
            pendingPosition = OverlayPosition.CUSTOM;
            grabOffsetX = (int) Math.round(mouseX) - anchorX;
            grabOffsetY = (int) Math.round(mouseY) - anchorY;
            return true;
        }
        return false;
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

    @Override
    public void onClose() {
        // If the editor wasn't committed (Cancel / Escape), undo any live scale preview.
        if (!committed) {
            CommonConfig.setSpriteScaleFactorTransient(originalScale);
        }
        super.onClose();
    }

    private boolean isInsideOverlay(double mouseX, double mouseY) {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return false;
        int w = contentW(player);
        int h = contentH(player);
        return mouseX >= anchorX && mouseX <= anchorX + w && mouseY >= anchorY && mouseY <= anchorY + h;
    }

    /** Keeps the content box fully on screen. */
    private void clampAnchor() {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        int w = player != null ? contentW(player) : 0;
        int h = player != null ? contentH(player) : 0;
        anchorX = OverlayAnchor.clamp(anchorX, 0, Math.max(0, this.width - w));
        anchorY = OverlayAnchor.clamp(anchorY, 0, Math.max(0, this.height - h));
    }

    /** Width of the active overlay's content box in GUI pixels (sprite composite or tooltip box). */
    private int contentW(Player player) {
        return detailed ? TankTooltipOverlay.tooltipWidthPx(player) : TankSpriteOverlay.compositeWidthPx(player);
    }

    /** Height of the active overlay's content box in GUI pixels. */
    private int contentH(Player player) {
        return detailed ? TankTooltipOverlay.tooltipHeightPx(player) : TankSpriteOverlay.compositeHeightPx();
    }

    /** Where the active overlay currently anchors, so the editor opens without the preview jumping. */
    private int[] currentAnchor(Player player) {
        return detailed ? TankTooltipOverlay.currentAnchor(player, this.width, this.height)
                        : TankSpriteOverlay.currentAnchor(player, this.width, this.height);
    }

    @Override
    public boolean isPauseScreen() {
        // Keep the world rendering so the player can see the overlay in context.
        return false;
    }

    /** Slider mapping its 0..1 value onto the {@link #MIN_SCALE}..{@link #MAX_SCALE} scale range. */
    private static final class ScaleSlider extends AbstractSliderButton {
        ScaleSlider(int x, int y, int width, int height, double initialScale) {
            super(x, y, width, height, Component.empty(), toSliderValue(initialScale));
            updateMessage();
        }

        private static double toSliderValue(double scale) {
            return Mth.clamp((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE), 0.0, 1.0);
        }

        private double toScale() {
            return MIN_SCALE + this.value * (MAX_SCALE - MIN_SCALE);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(String.format("Tank Scale: %.2fx", toScale())));
        }

        @Override
        protected void applyValue() {
            // Live preview only; persisted when the editor commits.
            CommonConfig.setSpriteScaleFactorTransient(toScale());
        }
    }
}
