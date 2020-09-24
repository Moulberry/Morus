package io.github.moulberry.morus.gui;

import org.lwjgl.util.vector.Vector2f;

import java.io.Serializable;

public class GuiAnchor implements Serializable {

    public enum AnchorPoint {
        TOPLEFT(0, 0), TOPMID(0.5f, 0), TOPRIGHT(1, 0),
        MIDRIGHT(1, 0.5f), BOTRIGHT(1, 1), BOTMID(0.5f, 1),
        BOTLEFT(0, 1), MIDLEFT(0, 0.5f), MIDMID(0.5f, 0.5f);

        public final float x;
        public final float y;

        AnchorPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public AnchorPoint anchorPoint;
    public Vector2f offset;
    public boolean inventoryRelative;

    public GuiAnchor(AnchorPoint anchorPoint, Vector2f offset) {
        this(anchorPoint, offset, false);
    }

    public GuiAnchor(AnchorPoint anchorPoint, Vector2f offset, boolean inventoryRelative) {
        this.anchorPoint = anchorPoint;
        this.offset = offset;
        this.inventoryRelative = inventoryRelative;
    }

    public GuiAnchor clone() {
        return new GuiAnchor(anchorPoint, new Vector2f(offset), inventoryRelative);
    }

    public static GuiAnchor createFromString(String str) {
        if(str == null || str.split(":").length != 4) {
            return null;
        }

        try {
            String[] split = str.split(":");
            AnchorPoint point = GuiAnchor.AnchorPoint.valueOf(split[0].toUpperCase());
            Vector2f pos = new Vector2f(Float.valueOf(split[1]), Float.valueOf(split[2]));
            boolean inventoryRelative = Boolean.valueOf(split[3]);
            return new GuiAnchor(point, pos, inventoryRelative);
        } catch(Exception e) { return null; }
    }

    @Override
    public String toString() {
        return anchorPoint.toString() + ":" + offset.x + ":" + offset.y + ":" + inventoryRelative;
    }
}
