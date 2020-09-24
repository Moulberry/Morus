package io.github.moulberry.morus.utils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Matrix4f;
import org.lwjgl.opengl.GL11;

public class FontUtils {

    public static void drawStringF(String str, FontRenderer fr, float x, float y, boolean shadow, int colour) {
        GL11.glTranslatef(x, y, 0);
        fr.drawString(str, 0, 0, colour, shadow);
        GL11.glTranslatef(-x, -y, 0);
    }

    public static void drawStringScaledMaxWidth(String str, FontRenderer fr, float x, float y, boolean shadow, int len, int colour) {
        int strLen = fr.getStringWidth(str);
        float factor = len/(float)strLen;
        factor = Math.min(1, factor);

        drawStringScaled(str, fr, x, y, shadow, colour, factor);
    }

    public static void drawStringCentered(String str, FontRenderer fr, float x, float y, boolean shadow, int colour) {
        int strLen = fr.getStringWidth(str);

        float x2 = x - strLen/2f;
        float y2 = y - fr.FONT_HEIGHT/2f;

        GL11.glTranslatef(x2, y2, 0);
        fr.drawString(str, 0, 0, colour, shadow);
        GL11.glTranslatef(-x2, -y2, 0);
    }

    public static void drawStringScaled(String str, FontRenderer fr, float x, float y, boolean shadow, int colour, float factor) {
        GlStateManager.scale(factor, factor, 1);
        fr.drawString(str, x/factor, y/factor, colour, shadow);
        GlStateManager.scale(1/factor, 1/factor, 1);
    }

    public static void drawStringCenteredScaledMaxWidth(String str, FontRenderer fr, float x, float y, boolean shadow, int len, int colour) {
        int strLen = fr.getStringWidth(str);
        float factor = len/(float)strLen;
        factor = Math.min(1, factor);
        int newLen = Math.min(strLen, len);

        float fontHeight = 8*factor;

        drawStringScaled(str, fr, x-newLen/2, y-fontHeight/2, shadow, colour, factor);
    }

    public static void drawStringCenteredScaled(String str, FontRenderer fr, float x, float y, boolean shadow, int len, int colour) {
        int strLen = fr.getStringWidth(str);
        float factor = len/(float)strLen;
        float fontHeight = 8*factor;

        drawStringScaled(str, fr, x-len/2, y-fontHeight/2, shadow, colour, factor);
    }

    public static void drawStringCenteredYScaled(String str, FontRenderer fr, float x, float y, boolean shadow, int len, int colour) {
        int strLen = fr.getStringWidth(str);
        float factor = len/(float)strLen;
        float fontHeight = 8*factor;

        drawStringScaled(str, fr, x, y-fontHeight/2, shadow, colour, factor);
    }

    public static void drawStringCenteredYScaledMaxWidth(String str, FontRenderer fr, float x, float y, boolean shadow, int len, int colour) {
        int strLen = fr.getStringWidth(str);
        float factor = len/(float)strLen;
        factor = Math.min(1, factor);
        float fontHeight = 8*factor;

        drawStringScaled(str, fr, x, y-fontHeight/2, shadow, colour, factor);
    }

}
