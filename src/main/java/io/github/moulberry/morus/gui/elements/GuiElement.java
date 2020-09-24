package io.github.moulberry.morus.gui.elements;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.Gui;
import org.lwjgl.util.vector.Vector2f;

import java.util.LinkedList;

public abstract class GuiElement extends Gui {

    public abstract int getWidth();
    public abstract int getHeight();
    public abstract void recalculate();
    public abstract void mouseClick(float x, float y, int mouseX, int mouseY);
    public abstract void mouseClickOutside();
    public abstract void render(float x, float y);

    public abstract GuiElement clone();
    public abstract JsonObject serialize();
}
