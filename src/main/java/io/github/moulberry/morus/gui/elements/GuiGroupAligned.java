package io.github.moulberry.morus.gui.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.morus.MorusSerializer;
import io.github.moulberry.morus.gui.GUIEditor;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class GuiGroupAligned extends GuiGroup {

    //Serialized
    private List<GuiElement> children;
    private boolean vertical;
    private int padding;

    public GuiGroupAligned(List<GuiElement> children, boolean vertical, int padding) {
        this.children = children;
        this.vertical = vertical;
        this.padding = padding;
        recalculate();
    }

    @Override
    public GUIEditor<?> createEditor(String screenName, GuiGroupScreen mainScreen, LinkedList<GuiGroup> editQueue) {
        return null;
    }

    @Override
    public GuiElement clone() {
        List<GuiElement> newChildren = new ArrayList<>();
        for(GuiElement child : children) {
            newChildren.add(child.clone());
        }
        return new GuiGroupAligned(newChildren, vertical, padding);
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        JsonArray children = new JsonArray();

        for(GuiElement childElement : this.children) {
            JsonObject child = MorusSerializer.boxType(childElement);
            if(child == null) continue;
            children.add(child);
        }

        object.add("children", children);
        object.addProperty("vertical", vertical);
        object.addProperty("padding", padding);
        return object;
    }

    @Override
    public void addChild(GuiElement element) {
        children.add(element);
    }

    @Override
    public boolean removeChild(GuiElement element) {
        return children.remove(element);
    }

    @Override
    public boolean replaceChild(GuiElement elementToReplace, GuiElement newElement) {
        int childIndex = children.indexOf(elementToReplace);
        if(childIndex >= 0) {
            children.remove(childIndex);
            children.add(childIndex, newElement);
            return true;
        }
        return false;
    }

    public int getPadding() {
        return padding;
    }

    public Collection<GuiElement> getChildren() {
        return children;
    }

    public void recalculate() {
        for(GuiElement child : children) {
            child.recalculate();
        }

        if(vertical) {
            height = 0;
            for(int i=0; i<children.size(); i++) {
                GuiElement child = children.get(i);
                childrenPosition.put(child, new Vector2f(0, height));
                height += child.getHeight();
                if(i != children.size()-1) height += getPadding();
            }

            width = 0;
            for(GuiElement child : children) {
                int childWidth = child.getWidth();
                if(childWidth > width) {
                    width = childWidth;
                }
            }
        } else {
            width = 0;
            for(int i=0; i<children.size(); i++) {
                GuiElement child = children.get(i);
                childrenPosition.put(child, new Vector2f(width, 0));
                width += child.getWidth();
                if(i != children.size()-1) width += getPadding();
            }

            height = 0;
            for(GuiElement child : children) {
                int childHeight = child.getHeight();
                if(childHeight > height) {
                    height = childHeight;
                }
            }
        }

    }

}
