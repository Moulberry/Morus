package io.github.moulberry.morus.gui.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.morus.MorusSerializer;
import io.github.moulberry.morus.gui.GUIEditor;
import io.github.moulberry.morus.gui.GUIEditorFloating;
import io.github.moulberry.morus.gui.GuiAnchor;
import io.github.moulberry.morus.gui.Resizable;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class GuiGroupFloating extends GuiGroup implements Resizable {

    //Serialized
    private LinkedHashMap<GuiElement, GuiAnchor> children;

    public GuiGroupFloating(int width, int height, LinkedHashMap<GuiElement, GuiAnchor> children) {
        this.width = width;
        this.height = height;
        this.children = children;
        recalculate();
    }

    @Override
    public GUIEditor<?> createEditor(String screenName, GuiGroupScreen mainScreen, LinkedList<GuiGroup> editQueue) {
        return new GUIEditorFloating(screenName, mainScreen, editQueue, this);
    }

    @Override
    public GuiElement clone() {
        LinkedHashMap<GuiElement, GuiAnchor> newChildren = new LinkedHashMap<>();
        for(Map.Entry<GuiElement, GuiAnchor> childEntry : getChildrenMap().entrySet()) {
            newChildren.put(childEntry.getKey().clone(), childEntry.getValue().clone());
        }
        return new GuiGroupFloating(width, height, newChildren);
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        JsonArray children = new JsonArray();

        for(Map.Entry<GuiElement, GuiAnchor> entry : getChildrenMap().entrySet()) {
            JsonObject child = MorusSerializer.boxType(entry.getKey());
            if(child == null) continue;

            JsonObject childObject = new JsonObject();
            childObject.addProperty("anchor", entry.getValue().toString());
            childObject.add("child", child);
            children.add(childObject);
        }

        object.add("children", children);
        object.addProperty("width", width);
        object.addProperty("height", height);
        return object;
    }

    @Override
    public void addChild(GuiElement element) {
        children.put(element, new GuiAnchor(GuiAnchor.AnchorPoint.BOTMID, new Vector2f()));
        recalculate();
    }

    @Override
    public boolean removeChild(GuiElement element) {
        childrenPosition.keySet().remove(element);

        return children.keySet().remove(element);
    }

    @Override
    public boolean replaceChild(GuiElement elementToReplace, GuiElement newElement) {
        GuiAnchor anchor = children.get(elementToReplace);

        if(anchor == null) {
            return false;
        }

        removeChild(elementToReplace);
        children.put(newElement, anchor);
        recalculate();

        return true;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Map<GuiElement, GuiAnchor> getChildrenMap() {
        return Collections.unmodifiableMap(children);
    }

    @Override
    public Map<GuiElement, Vector2f> getChildrenPosition() {
        return Collections.unmodifiableMap(childrenPosition);
    }

    @Override
    public void recalculate() {
        for(GuiElement child : children.keySet()) {
            child.recalculate();
        }

        for(Map.Entry<GuiElement, GuiAnchor> entry : children.entrySet()) {
            GuiElement child = entry.getKey();
            GuiAnchor guiAnchor = entry.getValue();
            float x = guiAnchor.anchorPoint.x * width - guiAnchor.anchorPoint.x * child.getWidth() + guiAnchor.offset.x;
            float y = guiAnchor.anchorPoint.y * height - guiAnchor.anchorPoint.y * child.getHeight() + guiAnchor.offset.y;

            childrenPosition.put(child, new Vector2f(x, y));
        }
    }

    @Override
    public Collection<GuiElement> getChildren() {
        return children.keySet();
    }
}
