package io.github.moulberry.morus.gui.elements;

import io.github.moulberry.morus.gui.GUIEditor;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public abstract class GuiGroup extends GuiElement {

    public int width;
    public int height;
    protected HashMap<GuiElement, Vector2f> childrenPosition = new HashMap<>();

    public GuiGroup() {
    }

    public abstract Collection<GuiElement> getChildren();
    public abstract boolean removeChild(GuiElement element);
    public abstract void addChild(GuiElement element);
    public abstract boolean replaceChild(GuiElement elementToReplace, GuiElement newElement);

    public abstract GUIEditor<?> createEditor(String screenName, GuiGroupScreen mainScreen, LinkedList<GuiGroup> editQueue);

    public Map<GuiElement, Vector2f> getChildrenPosition() {
        return Collections.unmodifiableMap(childrenPosition);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void mouseClick(float x, float y, int mouseX, int mouseY) {
        Map<GuiElement, Vector2f> childrenPos = getChildrenPosition();

        for(GuiElement child : getChildren()) {
            Vector2f childPos = childrenPos.get(child);
            if(mouseX > x+childPos.x && mouseX < x+childPos.x+child.getWidth()) {
                if(mouseY > y+childPos.y && mouseY < y+childPos.y+child.getHeight()) {
                    child.mouseClick(x+childPos.x, y+childPos.y, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void mouseClickOutside() {
        for(GuiElement child : getChildren()) {
            child.mouseClickOutside();
        }
    }

    @Override
    public void render(float x, float y) {
        Map<GuiElement, Vector2f> childrenPos = getChildrenPosition();

        for(GuiElement child : getChildren()) {
            Vector2f childPos = childrenPos.get(child);
            child.render(x+childPos.x, y+childPos.y);
        }
    }


}
