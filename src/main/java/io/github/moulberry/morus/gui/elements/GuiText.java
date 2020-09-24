package io.github.moulberry.morus.gui.elements;

import com.google.gson.JsonObject;
import io.github.moulberry.morus.MorusSubstitutor;
import io.github.moulberry.morus.gui.Resizable;
import io.github.moulberry.morus.utils.FontUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.LinkedList;

public class GuiText extends GuiElement implements Resizable {

    private String text;
    private float scale;

    public GuiText(String text) {
        this(text, 1);
    }

    public GuiText(String text, float scale) {
        this.text = text;
        this.scale = scale;
    }

    @Override
    public GuiElement clone() {
        return new GuiText(text, scale);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("text", text);
        json.addProperty("scale", scale);
        return json;
    }

    public FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public void setSize(int width, int height) {
        int textWidth = getUnscaledWidth();
        int textHeight = getUnscaledHeight();

        float horizontalScale = width/(float)textWidth;
        float verticalScale = height/(float)textHeight;

        scale = Math.min(horizontalScale, verticalScale);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getUnscaledWidth() {
        if(getFontRenderer() == null) return 0;
        return getFontRenderer().getStringWidth(getText());
    }

    public int getUnscaledHeight() {
        return 8;
    }

    @Override
    public int getWidth() {
        return Math.round(getUnscaledWidth()*scale);
    }

    @Override
    public int getHeight() {
        return Math.round(getUnscaledHeight()*scale);
    }

    @Override
    public void recalculate() {
    }

    @Override
    public void mouseClick(float x, float y, int mouseX, int mouseY) {
    }

    @Override
    public void mouseClickOutside() {
    }

    public String getText() {
        return MorusSubstitutor.processString(text);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextRaw() {
        return text;
    }

    @Override
    public void render(float x, float y) {
        if(getFontRenderer() == null) return;
        FontUtils.drawStringScaled(getText(), getFontRenderer(), x, y, true, new Color(255, 255, 255, 255).getRGB(), scale);
    }
}
