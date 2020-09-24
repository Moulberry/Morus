package io.github.moulberry.morus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.morus.gui.GuiAnchor;
import io.github.moulberry.morus.gui.elements.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MorusSerializer {

    private static final LinkedHashMap<Class<? extends GuiElement>, String> serializationMap = new LinkedHashMap<>();
    static {
        serializationMap.put(GuiGroupAligned.class, "groupAligned");
        serializationMap.put(GuiGroupFloating.class, "groupFloating");
        serializationMap.put(GuiText.class, "text");
    }

    public static GuiElement deserializeElement(JsonObject jsonObject) {
        String type = jsonObject.get("type").getAsString();
        JsonObject element = jsonObject.get("element").getAsJsonObject();

        switch(type) {
            case "groupFloating":
            {
                LinkedHashMap<GuiElement, GuiAnchor> children = new LinkedHashMap<>();
                JsonArray childrenArray = element.get("children").getAsJsonArray();
                for(JsonElement jsonElement : childrenArray) {
                    JsonObject childObject = jsonElement.getAsJsonObject();
                    String anchorS = childObject.get("anchor").getAsString();
                    JsonObject childElementJson = childObject.get("child").getAsJsonObject();

                    GuiAnchor anchor = GuiAnchor.createFromString(anchorS);
                    GuiElement child = deserializeElement(childElementJson);

                    if(child == null) continue;

                    children.put(child, anchor);
                }
                return new GuiGroupFloating(element.get("width").getAsInt(), element.get("height").getAsInt(), children);
            }
            case "text":
                String text = element.get("text").getAsString();
                float scale = element.get("scale").getAsFloat();
                GuiText gui = new GuiText(text);
                gui.setScale(scale);
                return gui;
            case "groupAligned":
                List<GuiElement> children = new ArrayList<>();
                JsonArray childrenArray = element.get("children").getAsJsonArray();
                for(JsonElement jsonElement : childrenArray) {
                    JsonObject childObject = jsonElement.getAsJsonObject();
                    GuiElement child = deserializeElement(childObject);
                    if(child == null) continue;
                    children.add(child);
                }
                return new GuiGroupAligned(children, element.get("vertical").getAsBoolean(), element.get("padding").getAsInt());
        }

        return null;
    }

    public static GuiGroupScreen deserializeScreen(JsonObject object) {
        try {
            LinkedHashMap<GuiElement, GuiAnchor> children = new LinkedHashMap<>();

            JsonArray mainScreen = object.get("mainScreen").getAsJsonArray();
            for(JsonElement jsonElement : mainScreen) {
                JsonObject childObject = jsonElement.getAsJsonObject();
                String anchorS = childObject.get("anchor").getAsString();
                JsonObject childElementJson = childObject.get("child").getAsJsonObject();

                GuiAnchor anchor = GuiAnchor.createFromString(anchorS);
                GuiElement child = deserializeElement(childElementJson);

                if(child == null) continue;

                children.put(child, anchor);
            }

            return new GuiGroupScreen(children);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonObject boxType(GuiElement element) {
        if(!serializationMap.containsKey(element.getClass())) {
            System.out.println(element.getClass());
            return null;
        }

        JsonObject child = new JsonObject();
        child.addProperty("type", serializationMap.get(element.getClass()));
        child.add("element", element.serialize());
        return child;
    }

    public static JsonObject serializeScreen(GuiGroupScreen screen) {
        JsonObject object = new JsonObject();
        JsonArray mainScreen = new JsonArray();

        for(Map.Entry<GuiElement, GuiAnchor> entry : screen.getChildrenMap().entrySet()) {
            JsonObject child = boxType(entry.getKey());
            if(child == null) continue;

            JsonObject childObject = new JsonObject();
            childObject.addProperty("anchor", entry.getValue().toString());
            childObject.add("child", child);
            mainScreen.add(childObject);
        }

        object.add("mainScreen", mainScreen);
        return object;
    }

}
