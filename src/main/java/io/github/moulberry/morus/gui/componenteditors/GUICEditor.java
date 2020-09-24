package io.github.moulberry.morus.gui.componenteditors;

import com.google.gson.JsonObject;
import io.github.moulberry.morus.gui.GUIEditor;
import io.github.moulberry.morus.gui.elements.GuiElement;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public abstract class GUICEditor<T extends GuiElement> extends Gui {

    protected GUIEditor editor;
    protected T editingOriginal;
    protected T editingObject;

    public GUICEditor(GUIEditor editor, T editingOriginal) {
        this.editor = editor;
        this.editingOriginal = editingOriginal;
        this.editingObject = (T) editingOriginal.clone();
    }

    public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
    public void mouseReleased(int mouseX, int mouseY, int state) {}

    public void keyTyped(char typedChar, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            editor.closeComponentEditor(editingOriginal, editingObject);
        }
    }

}
