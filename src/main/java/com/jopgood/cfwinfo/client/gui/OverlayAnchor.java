package com.jopgood.cfwinfo.client.gui;

import com.jopgood.cfwinfo.common.config.CommonConfig;
import com.jopgood.cfwinfo.common.config.CommonConfig.OverlayPosition;

/**
 * Single source of truth for where the overlay is anchored on screen.
 *
 * <p>Everything works in GUI-scaled pixels — the same coordinate space that
 * {@code GuiGraphics#guiWidth()}/{@code guiHeight()} and {@code Screen} mouse events use. Because
 * the live HUD and the overlay editor both resolve their anchor through here (and render through
 * the same code), a position chosen in the editor renders in exactly the same place on the HUD —
 * no drift between "where you dropped it" and "where it shows up".
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
