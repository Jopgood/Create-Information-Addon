package com.jopgood.cfwinfo.client.gui;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.config.CommonConfig.OverlayPosition;

/**
 * Resolves where the overlay is anchored on screen, in GUI-scaled pixels — the coordinate space
 * shared by {@code GuiGraphics#guiWidth()}/{@code guiHeight()} and {@code Screen} mouse events.
 * The HUD and the editor both anchor through here so a position chosen in the editor matches the HUD.
 */
public final class OverlayAnchor {

    /** Distance from the screen edge for the preset corner positions, in GUI pixels. */
    public static final int MARGIN = 10;

    private OverlayAnchor() {}

    /**
     * Resolves the top-left anchor of the simplified overlay composite.
     *
     * @param position the configured overlay position
     * @param guiW     GUI-scaled screen width
     * @param guiH     GUI-scaled screen height
     * @param contentW rendered width of the whole composite in GUI pixels
     * @param contentH rendered height of the whole composite in GUI pixels
     * @return {@code [x, y]} top-left anchor in GUI pixels
     */
    public static int[] resolveSprite(OverlayPosition position, int guiW, int guiH, int contentW, int contentH) {
        return switch (position) {
            case TOP_LEFT -> new int[]{MARGIN, MARGIN};
            case TOP_RIGHT -> new int[]{guiW - contentW - MARGIN, MARGIN};
            case BOTTOM_LEFT -> new int[]{MARGIN, guiH - contentH - MARGIN};
            case BOTTOM_RIGHT -> new int[]{guiW - contentW - MARGIN, guiH - contentH - MARGIN};
            case CUSTOM -> new int[]{
                    clamp(CommonConfig.getCustomX(), 0, Math.max(0, guiW - contentW)),
                    clamp(CommonConfig.getCustomY(), 0, Math.max(0, guiH - contentH))
            };
        };
    }

    public static int clamp(int value, int lo, int hi) {
        return Math.max(lo, Math.min(hi, value));
    }
}
